package com.cmu.client;

import com.cmu.message.ClientServerMessage;
import com.cmu.message.Direction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ExecutionException;
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
        // initialize cs-messages
        clientServerMessages.add(new ClientServerMessage(clientName, "S1", 0L, Direction.REQUEST));
        clientServerMessages.add(new ClientServerMessage(clientName, "S2", 0L, Direction.REQUEST));
        clientServerMessages.add(new ClientServerMessage(clientName, "S3", 0L, Direction.REQUEST));
        // initialize server-ports
        List<Integer> serverPorts = new ArrayList<>();
        serverPorts.add(18749);
        serverPorts.add(18750);
        serverPorts.add(18751);
        try {
            while (true) {
                // send manually
                new Scanner(System.in).nextLine();
                // create three tasks to connect with different server replicas
                List<FutureTask<ClientServerMessage>> futureTasks = new ArrayList<>();
                for (int i = 0; i < clientServerMessages.size(); i++) {
                    FutureTask<ClientServerMessage> task = new FutureTask<>(
                            new MessageThread("127.0.0.1"
                                    , serverPorts.get(i)
                                    , clientServerMessages.get(i)));
                    futureTasks.add(task);
                    new Thread(task).start();
                }
                ClientServerMessage message = null;
                Set<Integer> set = new HashSet<>();
                // try to find the task with good end
                while (message == null) {
                    int i = 0;
                    while (!futureTasks.get(i).isDone() || set.contains(i)) {
                        i++;
                        if (i == futureTasks.size()) {
                            i = 0;
                        }
                    }
                    message = futureTasks.get(i).get();
                    if (message == null) {
                        set.add(i);
                        if (set.size() == futureTasks.size()) {
                            System.out.println("All the servers are dead!");
                            return;
                        }
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Client End!");
        }
    }
}
