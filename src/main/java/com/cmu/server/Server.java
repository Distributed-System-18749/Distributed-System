package com.cmu.server;

import com.cmu.message.ServerServerMessage;
import com.cmu.message.ClientServerMessage;
import com.cmu.message.Direction;
import com.cmu.message.HeartbeatMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static com.cmu.config.GlobalConfig.SERVER_PORT;

/**
 * @author gongyiming
 * @date 2021/9/2
 */
public class Server {

    private long myState = -1;

    private final int port;
    private final boolean primary;

    public Server(int port, boolean primary) {
        this.port = port;
        this.primary = primary;
    }

    public static void main(String[] args) {
        System.out.println("Launching the server!");
        boolean primary;
        primary = args[0].equals("true") ? true : false;
        Server server = new Server(SERVER_PORT, primary);
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
                    System.out.println("[" + System.currentTimeMillis() + "]" + " Received " + input);
                    System.out.println("[" + System.currentTimeMillis() + "]" + " my_state_"
                            + ((ClientServerMessage) input).getServerName() + " = " + myState + " before processing "
                            + input);
                    myState = ((ClientServerMessage) input).getRequestNum();
                    System.out.println("[" + System.currentTimeMillis() + "]" + " my_state_s1 = " + myState
                            + " after processing " + input);
                    ((ClientServerMessage) input).setDirection(Direction.REPLY);
                    System.out.println("[" + System.currentTimeMillis() + "]" + " Sending " + input);
                    objectOutputStream.writeObject(input);
                }
                // if server is primary need to sends my state
                if (this.primary) {
                    // sends myState to other server using server-server msg class
                } else {
                    // if server is not pirmary, receives checkpoint(copy) from the primary
                    if (input instanceof ServerServerMessage) {
                        // receives myState
                    }
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
