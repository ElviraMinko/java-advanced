package info.kgeorgiy.ja.minko.hello;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class HelloUDPNonblockingClient extends AbstractHelloUDPClient {
    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        ArrayList<Integer> list = new ArrayList<>(Collections.nCopies(threads, 1));
        try (Selector selector = Selector.open()) {
            for (int i = 1; i <= threads; i++) {
                DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.configureBlocking(false);
                datagramChannel.connect(inetSocketAddress);
                datagramChannel.register(selector, SelectionKey.OP_WRITE, i);
            }
            while (!selector.keys().isEmpty()) {
                if (selector.select(HelloUDPClient.SO_TIMEOUT) == 0) {
                    selector.keys().forEach(key -> key.interestOps(SelectionKey.OP_WRITE));
                }
                for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                    SelectionKey key = i.next();
                    try {
                        DatagramChannel channel = (DatagramChannel) key.channel();
                        int threadId = (Integer) key.attachment() - 1;
                        String message = createMessage(prefix, threadId + 1, list.get(threadId));
                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
                            channel.receive(buffer);
                            String response = Utils.CHARSET.decode(buffer.flip()).toString();
                            if (response.contains(message)) {
                                System.out.println("Received: " + response);
                                list.set(threadId, list.get(threadId) + 1);
                            }
                            key.interestOps(SelectionKey.OP_WRITE);
                            if (list.get(threadId) > requests) {
                                channel.close();
                            }
                        } else {
                            channel.send(ByteBuffer.wrap(message.getBytes(Utils.CHARSET)), inetSocketAddress);
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } finally {
                        i.remove();
                    }
                }

            }
        } catch (IOException e) {
            System.err.println("IOException handled");
        }

    }

    private String createMessage(String prefix, int numberThread, int numberRequest) {
        return String.join("", prefix, Integer.toString(numberThread), "_", Integer.toString(numberRequest));
    }
}
