package com.cmu.gfd;

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
 * @author Yihe Yang
 * @date 2021/10/10
 */

public class GlobalFaultDector {
    private int memberCount = 0;
    private String[] membership = new String[3];
    
    public void transfer() {
        InetAddress inet;
        Socket[] socket = new Socket[3];
        OutputStream[] outputStream = new OutputStream[3];
        InputStream[] inputStream = new InputStream[3];
        ObjectOutputStream[] objectOutputStream = new ObjectOutputStream[3];
        ObjectInputStream[] objectInputStream = new ObjectInputStream[3];
        HeartbeatMessage[] message = new HeartbeatMessage[3];
        message[0] = new HeartbeatMessage(1, 1);
        message[1] = new HeartbeatMessage(2, 1);
        message[2] = new HeartbeatMessage(3, 1);

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
            try {
                inet = InetAddress.getByName("127.0.0.1");
                while (true) {
                    socket[0] = new Socket(inet, 15213);
                    socket[1] = new Socket(inet, 18613);
                    socket[2] = new Socket(inet, 14513);
                    
                    outputProcess(socket, outputStream, objectOutputStream, message, 0);
                    outputProcess(socket, outputStream, objectOutputStream, message, 1);
                    outputProcess(socket, outputStream, objectOutputStream, message, 2);

                    socket[0].shutdownOutput();
                    socket[1].shutdownOutput();
                    socket[2].shuedownOutput();

                    processInput(socket, inputStream, objectInputStream, 0);
                    processInput(socket, inputStream, objectInputStream, 1);
                    processInput(socket, inputStream, objectInputStream, 2);

                    memberModify();

                    socket[0].close();
                    socket[1].close();
                    socket[2].close();

                    Thread.sleep(heartbeatFreq);

                }
            } catch (IOException | InterruptedException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                closeAll(socket, outputStream, inputStream, objectOutputStream, objectInputStream, 0);
                closeAll(socket, outputStream, inputStream, objectOutputStream, objectInputStream, 1);
                closeAll(socket, outputStream, inputStream, objectOutputStream, objectInputStream, 2);
            }
        }
    }

    private void outputProcess(Socket[] sk, OutputStream[] os, ObjectOutputStream[] oos, HeartbeatMessage[] hm, int id) {
        os[id] = sk[id].getOutputStream();
        oos[id] = new objectOutputStream(os[id]);
        oos[id].writeObject(hm[id]);
        System.out.println(System.currentTimeMillis() + " " + hm[id] + " Sent");
    }

    private void processInput(Socket[] sk, InputStream[] is, ObjectInputStream[] ois, int id) {
        is[id] = sk[id].getInputStream();
        ois[id] = new objectInputStream(is[id]);
        Object input = ois[id].readObject();
        if (input instanceof HeartbeatMessage) {
            System.out.println(System.currentTimeMillis() + " " + input + " Received");
            message[id].incNum();
        }
    }

    private void closeAll(Socket[] sk, OutputStream[] os, InputStream[] is, ObjectOutputStream[] oos, ObjectInputStream[] ois, int id) {
        if (sk[id] != null) {
            try {
                sk[id].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (os[id] != null) {
            try {
                os[id].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (is[id] != null) {
            try {
                is[id].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (ois[id] != null) {
            try {
                ois[id].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (oos[id] != null) {
            try {
                oos[id].close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void memberModify() {
        int id = 1;
        String s = "S2";
        memberCount++;
        membership[id] = s;
        memberCount--;
        membership[id] = null;
        String temp = String.format("GFD: %d member(s): ", memberCount);
        for (int i = 0; i < membership.length -1; i++) {
            if (membership == null) {
                continue;
            }
            temp = temp + membership[id] + ", ";
        }
        if (membership[2] != null) {
            temp = temp + "S3";
        }
        System.out.println(temp); 
    }
     
    public static void main(string[] args) {
        System.out.println("GFD: 0 members");
        GlobalFaultDector gfd = new GlobalFaultDector();
        gfd.transfer();
    }
 }