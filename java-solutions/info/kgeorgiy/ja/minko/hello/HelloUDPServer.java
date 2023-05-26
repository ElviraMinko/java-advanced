package info.kgeorgiy.ja.minko.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;

import static info.kgeorgiy.ja.minko.hello.Utils.*;

/**
 * HelloUDPServer class
 * <p>
 * Realizing {@code HelloServer} interface
 *
 * @author Minko Elvira
 */
public class HelloUDPServer extends AbstractHelloUDPServer {

    private DatagramSocket datagramSocket;

    @Override
    public void start(int port, int threads) {
        try {
            threadPool = Executors.newFixedThreadPool(threads);
            main = Executors.newSingleThreadExecutor();
            datagramSocket = new DatagramSocket(port);
            main.submit(() -> {
                try {
                    while (!Thread.interrupted()) {
                        DatagramPacket datagramPacket = createPacket(datagramSocket);
                        datagramSocket.receive(datagramPacket);
                        threadPool.submit(() -> {
                            String request = getString(datagramPacket);
                            try {
                                setString(datagramPacket, getAnswer(request));
                                datagramSocket.send(datagramPacket);
                            } catch (IOException e) {
                                System.err.println("IOException in sending message:" + getAnswer(request));
                            }
                        });
                    }
                } catch (SocketException e) {
                    System.err.println("Can't create packet");
                } catch (IOException e) {
                    System.err.println("IOException in receiving request");
                }
            });
        } catch (SocketException e) {
            System.err.println("Can't create socket");
        }
    }

    @Override
    public void close() {
        datagramSocket.close();
        super.close();
    }
}
