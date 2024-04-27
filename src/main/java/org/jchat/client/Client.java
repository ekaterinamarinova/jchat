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
import java.util.logging.Logger;

public class Client  {

    private static final Logger LOG = Logger.getLogger(Client.class.getName());

    public void start() {
        try(AsynchronousSocketChannel client = AsynchronousSocketChannel.open()) {
            client.connect(new InetSocketAddress("localhost", 8080)).get(5, TimeUnit.SECONDS);

            LOG.info("Connected to server. Sending message..");

            String message = "Hello from client!";

            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));

            Integer sent = client.write(buffer).get(5, TimeUnit.SECONDS);

            if (sent == -1) {
                LOG.severe("Error sending message");
            }

            buffer.clear();

            client.read(buffer).get(5, TimeUnit.SECONDS);
            buffer.flip();
            byte[] read = new byte[buffer.remaining()];
            buffer.get(read);

            LOG.info("Bytes read: " + Arrays.toString(read));
            buffer.clear();
        } catch (IOException | ExecutionException | InterruptedException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
