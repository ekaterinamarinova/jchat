package org.jchat;

import org.jchat.client.Client;
import org.jchat.server.Server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();

        // Start the server in a separate thread
        executorService.submit(() -> {
            new Server().start();
        });

        // Wait a bit to ensure the server is started
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Start the client in the main thread
        new Client().start();

        // Shutdown the executor service
        executorService.shutdown();
    }
}