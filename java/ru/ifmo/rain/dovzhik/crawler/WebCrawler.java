package ru.ifmo.rain.dovzhik.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int perHost;

    private final ExecutorService downloadersPool;
    private final ExecutorService extractorsPool;
    private ConcurrentMap<String, HostData> hosts;

    private class HostData {
        final Queue<Runnable> waiting;
        int cnt;

        HostData() {
            waiting = new ArrayDeque<>();
            cnt = 0;
        }

        private synchronized void addTask(Runnable task) {
            if (cnt < perHost) {
                ++cnt;
                downloadersPool.submit(task);
            } else {
                waiting.add(task);
            }
        }

        private synchronized void nextTask() {
            final Runnable other = waiting.poll();
            if (other != null) {
                downloadersPool.submit(other);
            } else {
                --cnt;
            }
        }
    }

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        downloadersPool = Executors.newFixedThreadPool(downloaders);
        extractorsPool = Executors.newFixedThreadPool(extractors);
        hosts = new ConcurrentHashMap<>();
    }

    public WebCrawler(int downloaders, int extractors, int perHost) throws IOException {
        this(new CachingDownloader(), downloaders, extractors, perHost);
    }

    private void extractLinks(final Document page, final int depth, final Set<String> good, final ConcurrentMap<String, IOException> bad,
                              final Phaser sync, final Set<String> used) {
        try {
            page.extractLinks().stream()
                    .filter(used::add)
                    .forEach(link -> addToDownload(link, depth, good, bad, sync, used));
        } catch (IOException ignored) {
        } finally {
            sync.arrive();
        }
    }

    private void addToDownload(final String url, final int depth, final Set<String> good, final ConcurrentMap<String, IOException> bad,
                               final Phaser sync, final Set<String> used) {
        try {
            final String host = URLUtils.getHost(url);
            final HostData data = hosts.computeIfAbsent(host, s -> new HostData());

            sync.register();
            data.addTask(() -> {
                try {
                    final Document page = downloader.download(url);
                    good.add(url);
                    if (depth > 1) {
                        sync.register();
                        extractorsPool.submit(() -> extractLinks(page, depth - 1, good, bad, sync, used));
                    }
                } catch (IOException e) {
                    bad.put(url, e);
                } finally {
                    sync.arrive();
                    data.nextTask();
                }
            });
        } catch (MalformedURLException e) {
            bad.put(url, e);
        }
    }

    @Override
    public Result download(String url, int depth) {
        final Set<String> good = ConcurrentHashMap.newKeySet();
        final ConcurrentMap<String, IOException> bad = new ConcurrentHashMap<>();
        final Set<String> used = ConcurrentHashMap.newKeySet();
        final Phaser sync = new Phaser(1);
        used.add(url);
        addToDownload(url, depth, good, bad, sync, used);
        sync.arriveAndAwaitAdvance();
        return new Result(new ArrayList<>(good), bad);
    }

    @Override
    public void close() {
        downloadersPool.shutdownNow();
        extractorsPool.shutdownNow();
    }

    private static int getDownloaders(final int[] bounds) {
        return (bounds.length > 1) ? bounds[0] : 16;
    }

    private static int getExtractors(final int[] bounds) {
        return (bounds.length > 2) ? bounds[2] : 16;
    }

    private static int getPerHost(final int[] bounds) {
        return (bounds.length > 3) ? bounds[2] : 20;
    }

    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 5) {
            System.out.println("From 2 to 5 arguments expected");
            return;
        }
        for (final String arg : args) {
            if (arg == null) {
                System.out.println("non-null arguments expected");
            }
        }
        int[] bounds = new int[args.length - 2];
        try {
            for (int i = 2; i < args.length; i++) {
                bounds[i - 2] = Integer.parseInt(args[i]);
            }
        } catch (NumberFormatException e) {
            System.out.println("The integer numbers expected in the bounds arguments: " + e.getMessage());
            return;
        }
        try (WebCrawler crawler = new WebCrawler(getDownloaders(bounds), getExtractors(bounds), getPerHost(bounds))) {
            crawler.download(args[0], Integer.parseInt(args[1]));
        } catch (IOException e) {
            System.out.println("Unable to create instance of CachingDownloader: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("The integer number expected in the depth argument: " + e.getMessage());
        }
    }
}
