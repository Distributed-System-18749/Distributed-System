package com.cmu.gfd;

import com.cmu.ldf.ActiveHeartBeatThread;
import com.cmu.message.MembershipMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GlobalFaultDetector {
    private Set<String> membership;
    private Map<String, Integer> lfdMap;
    private int heartbeatFreq;
    private int port;
    private String name;

    public GlobalFaultDetector() {
        lfdMap = new HashMap<>();
        lfdMap.put("lfd1", 18752);
        lfdMap.put("lfd2", 18753);
        lfdMap.put("lfd3", 18754);
        membership = new HashSet<>();
        this.heartbeatFreq = 5000;
        this.name = "GFD";
        this.port = 18755;
    }

    public Map<String, Integer> getLfdMap() {
        return lfdMap;
    }

    public int getMemberCount() {
        return membership.size();
    }

    public void printMembershipInfo() {
        System.out.println("GFD: " + getMemberCount() + " members: " + membership.toString());

    }

    /**
     * create heartbeat thread towards local fault detector
     * @param port lfd remote port
     * @param lfdName lfd name (replica name)
     * @return new ActiveHeartBeatThread
     */
    public ActiveHeartBeatThread sendHeartbeat(int port, String lfdName) {
        return new ActiveHeartBeatThread(this.heartbeatFreq, "127.0.0.1", port, lfdName, this.name);
    }

    /**
     * update the membership
     * @param serverName the replica which happens with membership change
     * @param addOrRemove true = add, false = remove
     */
    public void updateMembership(String serverName, boolean addOrRemove) {
        if (addOrRemove) {
            membership.add(serverName);
            printMembershipInfo();
        } else if (membership.contains(serverName)) {
            membership.remove(serverName);
            printMembershipInfo();
        }
    }

    public void listenToLFD() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            serverSocket = new ServerSocket(this.port);
            while (true) {
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                objectOutputStream = new ObjectOutputStream(outputStream);

                MembershipMessage input = (MembershipMessage) objectInputStream.readObject();

                System.out.println("Received: " + input);
                String serverName = input.getReplicaName();
                boolean addOrRemove = input.getAddOrRemove();
                updateMembership(serverName, addOrRemove);

                socket.shutdownOutput();
                socket.close();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Launching the GFD!");
        GlobalFaultDetector gfd = new GlobalFaultDetector();
        gfd.printMembershipInfo();
        Map<String, Integer> lfds = gfd.getLfdMap();
        // heartbeat different lfd
        for (String lfd : lfds.keySet()) {
            new Thread(gfd.sendHeartbeat(lfds.get(lfd), lfd)).start();
        }
        // listen to membership change from lfd
        gfd.listenToLFD();
    }
}
