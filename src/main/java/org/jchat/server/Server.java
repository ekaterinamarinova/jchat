package org.jchat.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    public void start() {
        try(final AsynchronousServerSocketChannel ass = AsynchronousServerSocketChannel.open(
                AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(10))
            )
        ) {
            ass.bind(new InetSocketAddress(8080));
            LOG.info("Server started, listening on port %p" + ass.getLocalAddress().toString());

            ass.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel client, Void unused) {
                    LOG.info("Client request accepted, delegating...");
                    ass.accept(null, this);
                    //handle client
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    client.read(buffer, null, new CompletionHandler<Integer, Void>() {
                        @Override
                        public void completed(Integer numBytesRead, Void unused) {
                            //process read data
                            if (numBytesRead == -1) {
                                LOG.severe("Failed to read bytes into buffer!");
                                return;
                            }

                            buffer.flip();
                            byte[] bytesRead = new byte[numBytesRead];
                            buffer.get(bytesRead);

                            LOG.info("Received message from client: " + new String(bytesRead));

                            //flip buffer to make ready for writing
                            buffer.flip();

                            //respond echo
                            client.write(buffer, 2, TimeUnit.SECONDS, null, new CompletionHandler<Integer, Void>() {

                                @Override
                                public void completed(Integer bytesWritten, Void unused) {
                                    if (bytesWritten == -1) {
                                        //bad
                                        LOG.severe("Failed to write back echo. ");
                                        return;
                                    }

                                    LOG.info("Successfully written to the client");
                                }

                                @Override
                                public void failed(Throwable throwable, Void unused) {
                                    LOG.severe("Writing back to client failed");
                                }

                            });
                        }

                        @Override
                        public void failed(Throwable throwable, Void unused) {
                            LOG.severe("Error reading into client buffer. " + throwable.getMessage());
                        }
                    });
                }

                @Override
                public void failed(Throwable throwable, Void unused) {
                    LOG.severe("Error accepting connection! " + Arrays.toString(throwable.getStackTrace()));
                }
            });

            Thread.currentThread().join();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
