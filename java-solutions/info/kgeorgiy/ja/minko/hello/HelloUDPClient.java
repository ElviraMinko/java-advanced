package info.kgeorgiy.ja.minko.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static info.kgeorgiy.ja.minko.hello.Utils.*;

/**
 * HelloUDPClient class
 * <p>
 * Realizing {@code HelloClient} interface
 *
 * @author Minko Elvira
 */
public class HelloUDPClient implements HelloClient {

    public static final int SO_TIMEOUT = 239;

    /**
     * Static entry-point
     *
     * <p> All arguments have to be defined (not null).
     *
     * @param args array with given arguments.
     */
    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Incorrect format of command line arguments");
            return;
        }
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String prefix = args[2];
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);
            HelloClient helloUDPClient = new HelloUDPClient();
            helloUDPClient.run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println("Can't parse number: " + e.getMessage());
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {

        try (ExecutorService threadPool = Executors.newFixedThreadPool(threads)) {
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
    private boolean isTimeOut(ExecutorService threadPool) throws InterruptedException {
        return !threadPool.awaitTermination(100L, TimeUnit.SECONDS);
    }
}
