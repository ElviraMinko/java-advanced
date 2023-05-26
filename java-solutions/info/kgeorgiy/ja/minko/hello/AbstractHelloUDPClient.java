package info.kgeorgiy.ja.minko.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractHelloUDPClient implements HelloClient {


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


    protected boolean isTimeOut(ExecutorService threadPool) throws InterruptedException {
        return !threadPool.awaitTermination(100L, TimeUnit.SECONDS);
    }

}
