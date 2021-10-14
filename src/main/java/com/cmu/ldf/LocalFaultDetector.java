package com.cmu.ldf;

import java.util.Scanner;

/**
 * @author gongyiming
 * @date 2021/9/2
 */
public class LocalFaultDetector {
    private int port;
    private int serverPort;
    private String name;
    private String serverName;

    public LocalFaultDetector(String name, int port, String serverName, int serverPort) {
        this.name = name;
        this.port = port;
        this.serverPort = serverPort;
        this.serverName = serverName;
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Please enter correct args: lfdName lfdPort serverName serverPort");
            return;
        }
        System.out.println("Launching the LDF!");
        LocalFaultDetector localFaultDetector = new LocalFaultDetector(args[0],
                Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
        localFaultDetector.transfer();
    }

    public void transfer() {
        boolean check = true;
        int heartbeatFreq = 1000;
        // input the heartbeat frequency
        while (check) {
            System.out.print("Scan the heartbeat frequency you need: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine().trim();
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
        // open heartbeat entry point for GFD
        new Thread(new PassiveHeartBeatThread(this.port)).start();
        // start heartbeat local server replica
        new Thread(new ActiveHeartBeatAndReportThread(heartbeatFreq
                , "127.0.0.1"
                , serverPort
                , serverName
                , name
                , "127.0.0.1"
                , 18755)).start();
    }
}
