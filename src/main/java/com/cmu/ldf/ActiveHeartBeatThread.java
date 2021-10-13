package com.cmu.ldf;

import com.cmu.message.HeartbeatMessage;
import com.cmu.message.MembershipMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author gongyiming
 */
public class ActiveHeartBeatThread implements Runnable, Report{

    private final int heartbeatFreq;

    private final String remoteAddress;

    private final int remotePort;

    private final int replicaId;

    /**
     * true = live, false = dead
     */
    private boolean replicaStatus;

    /**
     * initialize the ActiveHeartBeatThread with the heartbeat frequency, remote address, port number and replica Id
     * @param heartbeatFreq heartbeatFreq heartbeat frequency
     * @param remoteAddress remoteAddress remote address without port number
     * @param remotePort remote port number
     * @param replicaId replica id binds with
     */
    public ActiveHeartBeatThread(int heartbeatFreq, String remoteAddress, int remotePort, int replicaId) {
        this.heartbeatFreq = heartbeatFreq;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.replicaId = replicaId;
        replicaStatus = false;
    }

    @Override
    public void run() {
        InetAddress inet;
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        HeartbeatMessage message = new HeartbeatMessage(replicaId, 1);
        while (true) {
            boolean check = true;
            try {
                inet = InetAddress.getByName(remoteAddress);
                socket = new Socket(inet, remotePort);
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(message);
                System.out.println(System.currentTimeMillis() + " " + message + " Sent");

                socket.shutdownOutput();

                inputStream = socket.getInputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                Object input = objectInputStream.readObject();
                if (input instanceof HeartbeatMessage) {
                    System.out.println(System.currentTimeMillis() + " " + input + " Received");
                    message.incNum();
                }
                socket.close();
            } catch (IOException | ClassNotFoundException e) {
                check = false;
                System.out.println("HeartBeating the replica" + replicaId + " failed. Now try again.");
            } finally {
                if (check != replicaStatus) {
                    replicaStatus = check;
                    MembershipMessage membershipMessage = new MembershipMessage(replicaId, replicaStatus);
                    // report replica status change to the higher level
                }
                try {
                    Thread.sleep(heartbeatFreq);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (socket != null) {
                    try {
                        socket.close();
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
            }
        }
    }
}
