package info.kgeorgiy.ja.minko.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractHelloUDPServer implements HelloServer {

    public static final long TIMEOUT = 239L;
    protected ExecutorService threadPool;
    protected ExecutorService main;

    protected static String getAnswer(String message) {
        return String.format("Hello, %s", message);
    }


    /**
     * Static entry-point
     *
     * <p> All arguments have to be defined (not null).
     *
     * @param args array with given arguments.
     */
    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.err.println("Incorrect format of command line arguments");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            try (HelloUDPServer helloUDPServer = new HelloUDPServer()) {
                helloUDPServer.start(port, threads);
                // :NOTE: мгновенное отключение сервера (для ожидания можно прочитать что-нибудь с консоли)
            }
        } catch (NumberFormatException e) {
            System.err.println("Can't parse number: " + e.getMessage());
        }
    }


    @Override
    public void close() {
        try {
            main.shutdown();
            threadPool.shutdown();
            if (!(threadPool.awaitTermination(TIMEOUT, TimeUnit.SECONDS)
                    && main.awaitTermination(TIMEOUT, TimeUnit.SECONDS))) {
                System.err.println("Too long closing");
            }
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted while waiting termination");
        }
    }
}
