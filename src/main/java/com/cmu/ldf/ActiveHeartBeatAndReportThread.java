package com.cmu.ldf;

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
public class ActiveHeartBeatAndReportThread extends ActiveHeartBeatThread implements Report{

    private final String reportAddress;

    private final int reportPort;
    /**
     * initialize the ActiveHeartBeatAndReportThread with the heartbeat frequency,
     * remote address, port number and replica Id
     * @param heartbeatFreq heartbeatFreq heartbeat frequency
     * @param remoteAddress remoteAddress remote address without port number
     * @param remotePort remote port number
     * @param replicaName replica name
     * @param sourceName the name of this heartbeat thread
     * @param reportAddress report address when reporting membership change
     * @param reportPort report port number when reporting membership change
     */
    public ActiveHeartBeatAndReportThread(int heartbeatFreq, String remoteAddress, int remotePort
            , String replicaName, String sourceName, String reportAddress, int reportPort) {
        super(heartbeatFreq, remoteAddress, remotePort, replicaName, sourceName);
        this.reportAddress = reportAddress;
        this.reportPort = reportPort;
    }

    @Override
    public void report(MembershipMessage membershipMessage) {
        System.out.println("Now start to report membership change to: " + reportAddress + ":" + reportPort);
        InetAddress inet;
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            inet = InetAddress.getByName(reportAddress);
            socket = new Socket(inet, reportPort);
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(membershipMessage);
            System.out.println(System.currentTimeMillis() + " " + membershipMessage + " Sent");

            socket.shutdownOutput();

            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
            Object input = objectInputStream.readObject();
            if (input instanceof MembershipMessage) {
                System.out.println(System.currentTimeMillis() + " " + input + " Received");
            }
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
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
        }
    }
}
