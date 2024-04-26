package org.jchat.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Client  {


    public void start() {
        try(AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
            client.connect(new InetSocketAddress("localhost", 8080)).get(5, TimeUnit.SECONDS);

            System.out.println("Connected to server. Sending message..");

            String message = "Hello from client!";

            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));

            Integer sent = client.write(buffer).get(5, TimeUnit.SECONDS);

            if (sent == -1) {
                System.out.println("Error sending message");
            }

            buffer.clear();

            client.read(buffer).get(5, TimeUnit.SECONDS);
            buffer.flip();
            byte[] read = new byte[buffer.remaining()];
            buffer.get(read);

            System.out.println("Bytes read: " + Arrays.toString(read));
            buffer.clear();
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
