package com.cmu.client;

import com.cmu.message.ClientServerMessage;
import com.cmu.message.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author gongyiming
 * @date 2021/9/2
 */
@Data
@AllArgsConstructor
public class Client {

    private String clientName;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Please enter one client name!");
            return;
        }
        Client client = new Client(args[0]);
        System.out.println("Launching the Client!");
        client.transfer();
    }

    private void transfer() {
        InetAddress inet;
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        ClientServerMessage message = new ClientServerMessage(clientName,  "S1", 0L, Direction.REQUEST);
        try {
            inet = InetAddress.getByName("127.0.0.1");
            while (true) {
                socket = new Socket(inet, 18749);
                outputStream = socket.getOutputStream();
                objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(message);
                System.out.println("[" + System.currentTimeMillis() + "]" + " Sent " + message);

                socket.shutdownOutput();

                inputStream = socket.getInputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                Object input = objectInputStream.readObject();
                if (input instanceof ClientServerMessage) {
                    System.out.println("[" + System.currentTimeMillis() + "]" + " Received " + input);
                    message.incRequestNum();
                }

                socket.close();
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            e.printStackTrace();
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

            System.out.println("Client End!");
        }
    }
}
