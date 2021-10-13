package com.cmu.gfd;

import com.cmu.ldf.ActiveHeartBeatAndReportThread;
import com.cmu.ldf.ActiveHeartBeatThread;
import com.cmu.ldf.LocalFaultDetector;
import com.cmu.message.ClientServerMessage;
import com.cmu.message.Direction;
import com.cmu.message.HeartbeatMessage;
import com.cmu.message.MembershipMessage;
import com.cmu.server.Server;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class GlobalFaultDetector {
    Set<String> membership;
    Map<String, Integer> lfdMap;
    int heartbeatFreq;
    int port;
    String name;

    public GlobalFaultDetector() {
        lfdMap = new HashMap<>();
        lfdMap.put("lfd1", 18752);
        lfdMap.put("lfd2", 18753);
        lfdMap.put("lfd3", 18754);
        membership = new HashSet<>();
        this.heartbeatFreq = 2000;
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

    public ActiveHeartBeatThread sendHeartbeat(int port, String lfdName) {
        return new ActiveHeartBeatThread(this.heartbeatFreq, "127.0.0.1", port, lfdName, this.name);
    }

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
        for (String lfd : lfds.keySet()) {
            new Thread(gfd.sendHeartbeat(lfds.get(lfd), lfd)).start();
        }
        gfd.listenToLFD();
    }
}
