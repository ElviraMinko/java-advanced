package info.kgeorgiy.ja.minko.crawler;

import info.kgeorgiy.java.advanced.crawler.*;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;

/**
 * WebCrawler class
 * <p>
 * Realizing {@code Crawler} interface
 *
 * @author Minko Elvira
 */
public class WebCrawler implements Crawler {

    private final Downloader downloader;
    private final ExecutorService downloadTreads;
    private final ExecutorService extractorTreads;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Runnable>> hosts = new ConcurrentHashMap<>();

    /**
     * Constructor from {@link WebCrawler}
     */
    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloadTreads = Executors.newFixedThreadPool(downloaders);
        this.extractorTreads = Executors.newFixedThreadPool(extractors);
    }

    @Override
    public Result download(String url, int depth) {
        Set<String> result = ConcurrentHashMap.newKeySet();
        Set<String> visitedLinks = ConcurrentHashMap.newKeySet();
        Set<String> newLayer = ConcurrentHashMap.newKeySet();
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        ConcurrentHashMap<String, IOException> exceptions = new ConcurrentHashMap<>();
        Phaser phaser = new Phaser(1);

        visitedLinks.add(url);
        queue.add(url);
        for (int i = depth; i >= 1; i--) {
            for (String link : queue) {
                downloadLinks(link, result, exceptions, visitedLinks, phaser, newLayer, i);
            }
            phaser.arriveAndAwaitAdvance();
            queue.clear();
            queue.addAll(newLayer);
            newLayer.clear();

        }
        return new Result(new ArrayList<>(result), exceptions);
    }

    private void downloadLinks(String url, Set<String> result, ConcurrentHashMap<String, IOException> exceptions, Set<String> visitedLinks, Phaser phaser, Set<String> newLayer, int depth) {
        try {
            String host = URLUtils.getHost(url);
            ConcurrentLinkedQueue<Runnable> hostWorker = hosts.computeIfAbsent(host, string -> new ConcurrentLinkedQueue<>());
            phaser.register();
            Runnable runnable = () -> {
                try {
                    Document document = downloader.download(url);
                    result.add(url);
                    if (depth != 1) {
                        phaser.register();
                        extractorTreads.submit(() -> {
                            try {
                                for (String link : document.extractLinks()) {
                                    if (!visitedLinks.contains(link)) {
                                        visitedLinks.add(link);
                                        newLayer.add(link);
                                    }
                                }
                            } catch (IOException e) {
                                System.err.println("Exception in extracting links:" + e.getMessage());
                            } finally {
                                phaser.arriveAndDeregister();
                            }
                        });
                    }
                } catch (IOException e) {
                    exceptions.put(url, e);
                } finally {
                    phaser.arriveAndDeregister();
                    Runnable task = hostWorker.poll();
                    if (task != null) {
                        downloadTreads.submit(task);
                    }
                }
            };

            try {
                downloadTreads.submit(runnable);
            } catch (RejectedExecutionException e) {
                hostWorker.add(runnable);
            }
        } catch (MalformedURLException e) {
            exceptions.put(url, e);
        }
    }


    @Override
    public void close() {
        extractorTreads.shutdown();
        downloadTreads.shutdown();
    }

    /**
     * Static entry-point
     *
     * <p> All arguments have to be defined (not null).
     *
     * @param args array with given arguments.
     * @throws NumberFormatException when incorrect args
     */
    public static void main(String[] args) throws IOException {
        if (args == null || args.length < 1 || args.length > 4) {
            System.err.println("Need to get arguments on this pattern: WebCrawler url [downloaders [extractors [perHost]]]");
            return;
        }
        int downloaders;
        int extractors;
        int perHost;
        try {
            downloaders = args.length > 1 ? Integer.parseInt(args[1]) : 4;
            extractors = args.length > 2 ? Integer.parseInt(args[2]) : 4;
            perHost = args.length > 3 ? Integer.parseInt(args[3]) : 4;
        } catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage());
        }
        int depth = 1;
        String url = args[0];
        if (url == null) {
            System.err.println("url can't be null");
            return;
        }

        Downloader downloader = new CachingDownloader(10.0);
        WebCrawler webCrawler = new WebCrawler(downloader, downloaders, extractors, perHost);
        webCrawler.download(url, depth);

    }
}