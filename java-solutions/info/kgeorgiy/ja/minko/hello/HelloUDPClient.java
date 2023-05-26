package info.kgeorgiy.ja.minko.hello;


import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static info.kgeorgiy.ja.minko.hello.Utils.*;

/**
 * HelloUDPClient class
 * <p>
 * Realizing {@code HelloClient} interface
 *
 * @author Minko Elvira
 */
public class HelloUDPClient extends AbstractHelloUDPClient {

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        ExecutorService threadPool = Executors.newFixedThreadPool(threads);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        for (int i = 1; i <= threads; i++) {
            final int finalI = i;
            threadPool.submit(() -> process(prefix, requests, inetSocketAddress, finalI));
        }

        try {
            threadPool.shutdown();
            if (isTimeOut(threadPool)) {
                System.err.println("Too long waiting");
                threadPool.shutdownNow();
                if (isTimeOut(threadPool)) {
                    System.err.println("Resource leak");
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Can't shutdown threads");
        }

    }

    private void process(String prefix, int requests, InetSocketAddress inetSocketAddress, int theadNumber) {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(SO_TIMEOUT);
            DatagramPacket datagramPacket = new DatagramPacket(new byte[0], 0, inetSocketAddress);
            for (int j = 1; j <= requests; j++) {
                String message = createMessage(prefix, theadNumber, j);
                while (!Thread.interrupted()) {
                    try {
                        setString(datagramPacket, message);
                        datagramSocket.send(datagramPacket);
                        DatagramPacket receivedDatagramPacket = createPacket(datagramSocket, inetSocketAddress);
                        datagramSocket.receive(receivedDatagramPacket);
                        String responseMessage = getString(receivedDatagramPacket);
                        if (responseMessage.contains(message)) {
                            System.out.println("Requested: " + message);
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println("IOException in sending " + message);
                    }
                }
            }
        } catch (SocketException e) {
            System.err.println("Can't create socket");
        }
    }

    private String createMessage(String prefix, int numberThread, int numberRequest) {
        return String.join("", prefix, Integer.toString(numberThread), "_", Integer.toString(numberRequest));
    }
}
