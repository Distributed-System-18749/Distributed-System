package com.cmu.server;

import com.cmu.ldf.ActiveCheckpointThread;
import com.cmu.message.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.cmu.config.GlobalConfig.SERVER_MAP;
import static com.cmu.config.GlobalConfig.SERVER_PORT;

/**
 * @author gongyiming
 * @date 2021/9/2
 */
public class Server {

    private long myState = -1;

    private final int port;
    private boolean isReady;
    private String name;

    public Server(String name, int port, boolean isReady) {
        this.port = port;
        this.isReady = isReady;
        this.name = name;
    }

    public static void main(String[] args) {
        System.out.println("Launching the server!");
        String myName = args[0];
        boolean isReady = true;
        if (args.length > 1) {
             isReady = false;
        }
        Server server = new Server(myName, SERVER_PORT, isReady);
        server.transfer();
    }

    public void transfer() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                objectOutputStream = new ObjectOutputStream(outputStream);

                Object input = objectInputStream.readObject();

                if (input instanceof HeartbeatMessage) {
                    System.out.println("[" + System.currentTimeMillis() + "] " + input + " Received");
                    ((HeartbeatMessage) input).setDirection(Direction.REPLY);
                    objectOutputStream.writeObject(input);
                    System.out.println("[" + System.currentTimeMillis() + "] " + input + " Sent");
                } else if (input instanceof ClientServerMessage) {
                    myState = ((ClientServerMessage) input).getRequestNum();
                    if (this.isReady) {
                        System.out.println("[" + System.currentTimeMillis() + "]" + " Received " + input);
                        System.out.println("[" + System.currentTimeMillis() + "]" + " my_state_" + ((ClientServerMessage) input).getServerName() + " = " + myState + " before processing " + input);
                        System.out.println("[" + System.currentTimeMillis() + "]" + " my_state_s1 = " + myState + " after processing " + input);
                        ((ClientServerMessage) input).setDirection(Direction.REPLY);
                        System.out.println("[" + System.currentTimeMillis() + "]" + " Sending " + input);
                        objectOutputStream.writeObject(input);
                    }
                } else if (input instanceof CheckpointMessage) {
                    long state = ((CheckpointMessage) input).getMyState();
                    myState = Math.max(state, myState);
                    System.out.println("[" + System.currentTimeMillis() + "]" + "Received checkpoint: " + input);
                    isReady = true;
                } else if (input instanceof PrimaryMessage) {
                    String replicaName = ((CheckpointMessage) input).getBackupName();
                    new Thread(new ActiveCheckpointThread(
                            SERVER_MAP.get(replicaName),
                            SERVER_PORT,
                            myState,
                            name,
                            replicaName
                    )).start();
                }

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
            System.out.println("Server End!");
        }

    }
}
