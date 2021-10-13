package com.cmu.ldf;

import java.util.Scanner;

/**
 * @author gongyiming
 * @date 2021/9/2
 */
public class LocalFaultDetector {

    public static void main(String[] args) {
        System.out.println("Launching the LDF!");
        LocalFaultDetector localFaultDetector = new LocalFaultDetector();
        localFaultDetector.transfer();
    }

    public void transfer() {
        boolean check = true;
        int heartbeatFreq = 1000;
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
        new Thread(new PassiveHeartBeatThread(18752)).start();
        // start heartbeat local server replica
        new Thread(new ActiveHeartBeatAndReportThread(heartbeatFreq
                , "127.0.0.1"
                , 18749
                , "server1"
                , "lfd1"
                , "127.0.0.1"
                , 18755)).start();
    }
}
