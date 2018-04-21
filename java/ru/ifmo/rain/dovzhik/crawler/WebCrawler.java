package ru.ifmo.rain.dovzhik.crawler;

import info.kgeorgiy.java.advanced.crawler.CachingDownloader;
import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int perHost;

    private final ExecutorService downloadersPool;
    private final ExecutorService extractorsPool;
    private final Map<String, Semaphore> hosts;
    private final Map<String, Document> downloadedPages;
    private final Map<String, List<String>> parsedPages;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        downloadersPool = Executors.newFixedThreadPool(downloaders);
        extractorsPool = Executors.newFixedThreadPool(extractors);
        hosts = new ConcurrentHashMap<>();
        downloadedPages = new ConcurrentHashMap<>();
        parsedPages = new ConcurrentHashMap<>();
    }

    public WebCrawler(int downloaders, int extractors, int perHost) throws IOException {
        this(new CachingDownloader(), downloaders, extractors, perHost);
    }

    public WebCrawler(int downloaders, int extractors) throws IOException {
        this(downloaders, extractors, 20);
    }

    public WebCrawler(int downloaders) throws IOException {
        this(downloaders, 16);
    }

    public WebCrawler() throws IOException {
        this(16);
    }

    private Document downloadPage(final String url) throws IOException {
        Semaphore semaphore = null;
        try {
            final String host = URLUtils.getHost(url);
            hosts.putIfAbsent(host, new Semaphore(perHost));
            semaphore = hosts.get(host);
            semaphore.acquireUninterruptibly();
            Document res = downloader.download(url);
            downloadedPages.putIfAbsent(url, res);
            return res;
        } finally {
            if (semaphore != null) {
                semaphore.release();
            }
        }
    }

    private Optional<Document> getPage(final String url, final Set<String> good, final Map<String, IOException> bad) {
        Document res = null;
        if (!downloadedPages.containsKey(url)) {
            try {
                res = downloadPage(url);
                good.add(url);
            } catch (IOException e) {
                bad.put(url, e);
            }
        } else {
            res = downloadedPages.get(url);
            good.add(url);
        }
        return Optional.ofNullable(res);
    }

    private List<String> getLinks(final String url, final Future<Optional<Document>> page) {
        List<String> res;
        if (!parsedPages.containsKey(url)) {
            try {
                res = page.get().map(doc -> {
                    try {
                        List<String> tmp = doc.extractLinks();
                        parsedPages.putIfAbsent(url, tmp);
                        return tmp;
                    } catch (IOException e) {
                        return null;
                    }
                }).orElse(Collections.emptyList());
            } catch (InterruptedException | ExecutionException e) {
                res = Collections.emptyList();
            }
        } else {
            res = parsedPages.get(url);
        }
        return res;
    }

    private Callable<List<String>> toCallableLinks(final String url, final Future<Optional<Document>> page) {
        return () -> getLinks(url, page);
    }

    private Callable<Optional<Document>> toCallablePage(final String url, final Set<String> good, final Map<String, IOException> bad) {
        return () -> getPage(url, good, bad);
    }

    private static List<String> safeGetLinks(final Future<List<String>> elem) {
        try {
            return elem.get();
        } catch (ExecutionException | InterruptedException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Result download(String url, int depth) {
        final Set<String> good = ConcurrentHashMap.newKeySet();
        final Map<String, IOException> bad = new ConcurrentHashMap<>();
        final Set<String> visited = new HashSet<>();
        final Queue<String> tmp = new ArrayDeque<>(depth);
        final Queue<String> que = new ArrayDeque<>(depth);
        que.add(url);
        visited.add(url);
        int curDepth = 1;
        while (!que.isEmpty() && curDepth < depth) {
            tmp.clear();
            que.stream()
                    .map(link -> toCallableLinks(link, downloadersPool.submit(() -> getPage(link, good, bad))))
                    .map(extractorsPool::submit)
                    .collect(Collectors.toList()).stream()
                    .map(WebCrawler::safeGetLinks)
                    .flatMap(Collection::stream)
                    .forEach(link -> {
                        if (!visited.contains(link)) {
                            tmp.add(link);
                            visited.add(link);
                        }
                    });
            que.clear();
            que.addAll(tmp);
            ++curDepth;
        }
        if (!que.isEmpty()) {
            que.stream()
                    .map(link -> toCallablePage(link, good, bad))
                    .map(downloadersPool::submit)
                    .collect(Collectors.toList())
                    .forEach(elem -> {
                        try {
                            elem.get();
                        } catch (ExecutionException | InterruptedException ignored) {
                        }
                    });
        }
        return new Result(new ArrayList<>(good), bad);
    }

    @Override
    public void close() {
        downloadersPool.shutdownNow();
        extractorsPool.shutdownNow();
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
        try (WebCrawler crawler = (bounds.length == 3) ? new WebCrawler(bounds[0], bounds[1], bounds[2])
                : (bounds.length == 2) ? new WebCrawler(bounds[0], bounds[1])
                : (bounds.length == 1) ? new WebCrawler(bounds[0])
                : new WebCrawler()) {
            crawler.download(args[0], Integer.parseInt(args[1]));
        } catch (IOException e) {
            System.out.println("Unable to create instance of CachingDownloader: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("The integer number expected in the depth argument: " + e.getMessage());
        }
    }
}
