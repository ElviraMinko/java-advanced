package info.kgeorgiy.ja.minko.hello;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.AbstractMap;
import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;

public class HelloUDPNonblockingServer extends AbstractHelloUDPServer {

    private Selector selector;

    private DatagramChannel datagramChannel;

    private final Deque<AbstractMap.SimpleEntry<ByteBuffer, SocketAddress>> queue = new ConcurrentLinkedDeque<>();
    public static void TryToCloseSelector(Selector selector){
        try {
            selector.close();
        } catch (IOException ex) {
            System.err.println("I/O error in close: " + ex.getMessage());
        }
    }
    public static void TryToCloseDatagramChannel(DatagramChannel datagramChannel){
        try {
            datagramChannel.close();
        } catch (IOException ex) {
            System.err.println("I/O error in close: " + ex.getMessage());
        }
    }
    @Override
    public void start(int port, int threads) {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.err.println("I/O error occurs: " + e.getMessage());
            return;
        }
        try {
            datagramChannel = DatagramChannel.open();
        } catch (IOException e) {
            System.err.println("I/O error occurs: " + e.getMessage());
            TryToCloseSelector(selector);
            return;
        }
        try {
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(new InetSocketAddress(port));
            datagramChannel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            TryToCloseSelector(selector);
            TryToCloseDatagramChannel(datagramChannel);
            return;
        }
        threadPool = Executors.newFixedThreadPool(threads);
        main = Executors.newSingleThreadExecutor();
        main.submit(() -> {
            while (!datagramChannel.socket().isClosed() && !Thread.interrupted()) {
                try {
                    if (selector.select() == 0) {
                        continue;
                    }
                } catch (IOException e) {
                    continue;
                }
                for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); i.remove()) {
                    SelectionKey key = i.next();
                    try {
                        DatagramChannel channel = (DatagramChannel) key.channel();
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
                            SocketAddress socketAddress = channel.receive(buffer);
                            threadPool.submit(() -> {
                                String message = "Hello, " + Utils.CHARSET.decode(buffer.flip());
                                queue.add(new AbstractMap.SimpleEntry<>(ByteBuffer.wrap(message.getBytes(Utils.CHARSET)), socketAddress));
                                key.interestOps(SelectionKey.OP_WRITE);
                                selector.wakeup();
                            });
                        } else {
                            if (!queue.isEmpty()) {
                                AbstractMap.SimpleEntry<ByteBuffer, SocketAddress> data = queue.poll();
                                datagramChannel.send(data.getKey(), data.getValue());
                            }
                            key.interestOpsOr(SelectionKey.OP_READ);
                        }

                    } catch (IOException e) {
                        System.err.println("I/O error occurs" + e.getMessage());
                    }
                }
            }
        });
    }


    @Override
    public void close() {
        try {
            datagramChannel.close();
            selector.close();
        } catch (IOException e) {
            System.err.println("Closing throws IOException:" + e.getMessage());
        }
        super.close();
    }
}
