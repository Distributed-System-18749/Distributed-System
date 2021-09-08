package com.cmu.ldf;

import com.cmu.message.HeartbeatMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author gongyiming
 * @date 2021/9/2
 */
public class LocalFaultDetector {

    //@Test
    public static void main(String[] args) {
        InetAddress inet;
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        HeartbeatMessage message = new HeartbeatMessage(1, 1);

        boolean check = true;
        int heartbeatFreq = 1000;
        while (check) {
            System.out.print("Scan the heartbeat frequency you need: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            try {
                heartbeatFreq = Integer.parseInt(input);
                if (heartbeatFreq <= 0) {
                    System.out.println("please input an unsigned integer!");
                } else {
                    check = false;
                }
            } catch (NumberFormatException e) {
                System.out.println("please input an unsigned integer!");
            }
        }

        try {
            inet = InetAddress.getByName("127.0.0.1");
            while (true) {
                socket = new Socket(inet, 18749);
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
                Thread.sleep(heartbeatFreq);
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
            System.out.println("Client End!");
        }
    }
}
