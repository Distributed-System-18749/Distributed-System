package com.cmu.ldf;

import com.cmu.message.CheckpointMessage;
import com.cmu.message.Direction;
import lombok.AllArgsConstructor;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

@AllArgsConstructor
public class ActiveCheckpointThread implements Runnable{

    private String replicaAddress;
    private int replicaPort;
    private long myState;
    private final String primaryName;
    private final String backupName;

    @Override
    public void run() {
        InetAddress inet;
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        // long myState = myStateReference[0];
        CheckpointMessage message = new CheckpointMessage(primaryName, backupName, myState);
        //ServerServerMessage message = new ServerServerMessage(primaryName, backupName, myState, 1L, Direction.REQUEST);

            try {
                inet = InetAddress.getByName(replicaAddress);
                socket = new Socket(inet, replicaPort);
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(message);
                System.out.println(System.currentTimeMillis() + " " + message + " Sent");
                socket.shutdownOutput();
                socket.close();
            } catch (IOException e) {
                System.out.println("Checkpoint the " + backupName + " failed.");
            } finally {
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
