package ru.ifmo.rain.dovzhik.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.Result;
import info.kgeorgiy.java.advanced.crawler.URLUtils;
import javafx.util.Pair;
import org.jsoup.select.Collector;

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
import java.util.function.Function;
import java.util.stream.Collectors;


public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int perHost;

    private final ExecutorService downloadersPool;
    private final ExecutorService extractorsPool;
    private final ConcurrentHashMap<String, Semaphore> hosts;
    private final ConcurrentHashMap<String, Document> downloadedPages;
    private final ConcurrentHashMap<String, List<String>> parsedPages;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        downloadersPool = Executors.newFixedThreadPool(downloaders);
        extractorsPool = Executors.newFixedThreadPool(extractors);
        hosts = new ConcurrentHashMap<>();
        downloadedPages = new ConcurrentHashMap<>();
        parsedPages = new ConcurrentHashMap<>();
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

    private List<String> getLinks(final Pair<String, Future<Optional<Document>>> data) {
        final String url = data.getKey();
        final Future<Optional<Document>> page = data.getValue();
        if (!parsedPages.containsKey(url)) {
            try {
                return page.get().map((doc) -> {
                    try {
                        return doc.extractLinks();
                    } catch (IOException e) {
                        return null;
                    }
                }).orElse(Collections.emptyList());
            } catch (InterruptedException | ExecutionException e) {
                return Collections.emptyList();
            }
        } else {
            return parsedPages.get(url);
        }
    }

    private Callable<List<String>> toCallableLinks(final Pair<String, Future<Optional<Document>>> data) {
        return () -> getLinks(data);
    }

    private Callable<Optional<Document>> toCallablePage(final String url, final Set<String> good, final Map<String, IOException> bad) {
        return () -> getPage(url, good, bad);
    }

    private static List<String> safeGet(final Future<List<String>> elem) {
        try {
            return elem.get();
        } catch (ExecutionException | InterruptedException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public Result download(String url, int depth) {
        final Set<String> good = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final Map<String, IOException> bad = new ConcurrentHashMap<>();
        final Set<String> visited = new HashSet<>();
        final Queue<String> tmp = new ArrayDeque<>(depth);
        final Queue<String> que = new ArrayDeque<>(depth);
        que.add(url);
        visited.add(url);
        int curDepth = 1;
        while (!que.isEmpty() && curDepth < depth) {
            try {
                extractorsPool.invokeAll(que.stream()
                        .map((link) -> new Pair<>(link, downloadersPool.submit(() -> getPage(link, good, bad))))
                        .map(this::toCallableLinks)
                        .collect(Collectors.toList())
                ).stream()
                        .map(WebCrawler::safeGet)
                        .flatMap(Collection::stream)
                        .forEach(link -> {
                            if (!visited.contains(link)) {
                                tmp.add(link);
                                visited.add(link);
                            }
                        });
            } catch (InterruptedException ignored) {
            }
            que.clear();
            que.addAll(tmp);
            tmp.clear();
            ++curDepth;
        }
        if (!que.isEmpty()) {
            try {
                downloadersPool.invokeAll(que.stream()
                        .map((link) -> toCallablePage(link, good, bad))
                        .collect(Collectors.toList())
                ).forEach((elem) -> {
                    try {
                        elem.get();
                    } catch (ExecutionException | InterruptedException ignored) {
                    }
                });
            } catch (InterruptedException ignored) {
            }
        }
        return new Result(new ArrayList<>(good), bad);
    }

    @Override
    public void close() {
        downloadersPool.shutdown();
        extractorsPool.shutdown();
    }
}
