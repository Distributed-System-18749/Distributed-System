package com.cmu.server;

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

/**
 * @author gongyiming
 * @date 2021/9/2
 */
public class Server {

    private long myState = -1;

    public static void main(String[] args) {
        System.out.println("Launching the server!");
        Server server = new Server();
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
            serverSocket = new ServerSocket(18749);
            while (true) {
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                objectOutputStream = new ObjectOutputStream(outputStream);

                Object input = objectInputStream.readObject();

                if (input instanceof HeartbeatMessage) {
                    System.out.println("[" + System.currentTimeMillis() + "] " + input + " Received");
                    objectOutputStream.writeObject(input);
                    System.out.println("[" + System.currentTimeMillis() + "] " + input + " Sent");
                } else if (input instanceof ClientServerMessage) {
                    System.out.println("[" + System.currentTimeMillis() + "]" + " Received " + input);
                    System.out.println("[" + System.currentTimeMillis() + "]" + " my_state_s1 = " + myState + " before processing " + input);
                    myState = ((ClientServerMessage) input).getRequestNum();
                    System.out.println("[" + System.currentTimeMillis() + "]" + " my_state_s1 = " + myState + " after processing " + input);
                    ((ClientServerMessage) input).setDirection(Direction.REPLY);
                    System.out.println("[" + System.currentTimeMillis() + "]" + " Sending " + input);
                    objectOutputStream.writeObject(input);
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
