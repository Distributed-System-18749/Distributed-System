package com.cmu.client;

import com.cmu.message.ClientServerMessage;
import com.cmu.message.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

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
        List<ClientServerMessage> clientServerMessages = new ArrayList<>();
        clientServerMessages.add(new ClientServerMessage(clientName, "S1", 0L, Direction.REQUEST));
        clientServerMessages.add(new ClientServerMessage(clientName, "S2", 0L, Direction.REQUEST));
        clientServerMessages.add(new ClientServerMessage(clientName, "S3", 0L, Direction.REQUEST));
        List<Integer> clientPorts = new ArrayList<>();
        clientPorts.add(18749);
        clientPorts.add(18750);
        clientPorts.add(18751);
        try {
            while (true) {
                //new Scanner(System.in).nextLine();
                List<FutureTask<ClientServerMessage>> futureTasks = new ArrayList<>();
                for (int i = 0; i < clientServerMessages.size(); i++) {
                    FutureTask<ClientServerMessage> task = new FutureTask<>(
                            new MessageThread("127.0.0.1"
                                    , clientPorts.get(i)
                                    , clientServerMessages.get(i)));
                    futureTasks.add(task);
                    new Thread(task).start();
                }
                int i = 0;
                while (!futureTasks.get(i).isDone()) {
                    i++;
                    if (i == futureTasks.size()) {
                        i = 0;
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Client End!");
        }
    }
}
