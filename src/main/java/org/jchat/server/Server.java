package org.jchat.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    public void start() {
        try(final AsynchronousServerSocketChannel ass = AsynchronousServerSocketChannel.open(
                AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(10))
            )
        ) {
            ass.bind(new InetSocketAddress(8080));

            System.out.println("Server started, listening on port: " + ass.getLocalAddress().toString());

            ass.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel client, Void unused) {
                    System.out.println("Client request accepted, delegating...");
                    ass.accept(null, this);
                    //handle client
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    client.read(buffer, null, new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(Integer numBytesRead, Void unused) {
                            //process read data
                            if (numBytesRead == -1) {
                                System.out.println("Failed to read bytes into buffer!");
                                return;
                            }

                            buffer.flip();
                            byte[] bytesRead = new byte[numBytesRead];
                            buffer.get(bytesRead);

                            System.out.println("Received message from client: " + new String(bytesRead));

                            //flip buffer to make ready for writing
                            buffer.flip();

                            //respond echo
                            client.write(buffer, 2, TimeUnit.SECONDS, null, new CompletionHandler<Integer, Void>() {

                                @Override
                                public void completed(Integer bytesWritten, Void unused) {
                                    if (bytesWritten == -1) {
                                        //bad
                                        System.out.println("Failed to write back echo. ");
                                        return;
                                    }

                                    System.out.println("Successfully written to the client");
                                }

                                @Override
                                public void failed(Throwable throwable, Void unused) {
                                    System.out.println("Writing back to client failed");
                                }

                            });
                        }

                        @Override
                        public void failed(Throwable throwable, Void unused) {
                            System.out.println("Error reading into client buffer. " + throwable.getMessage());
                        }
                    });
                }

                @Override
                public void failed(Throwable throwable, Void unused) {
                    System.out.println("Error accepting connection! " + Arrays.toString(throwable.getStackTrace()));
                }
            });

            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
