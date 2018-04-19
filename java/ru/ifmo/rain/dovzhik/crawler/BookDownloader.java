package ru.ifmo.rain.dovzhik.crawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class BookDownloader {
    public static void main(String[] args) {
        final String host;
        try {
            host = URLUtils.getHost("https://e.lanbook.com");
        } catch (MalformedURLException ignored) {
            System.err.println(ignored.getMessage());
            return;
        }
        final Set<String> books = ConcurrentHashMap.newKeySet();
        final Predicate<String> checker = new BookPredicate();
        final BiConsumer<String, String> handler = new BookAdder(books);
        final Downloader downloader;
        try {
            downloader = new HostCachedDownloader("tempPages", host, checker, handler);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }
        try (final Crawler crawler = new WebCrawler(downloader, 40, 40, 40)) {
            crawler.download("https://e.lanbook.com/books", 40);
            try (final BufferedWriter out = new BufferedWriter(Files.newBufferedWriter(Paths.get("books.txt")))){
                books.stream().sorted().forEach(book -> {
                    try {
                        out.write(book + System.lineSeparator());
                    } catch (IOException e) {
                        System.err.println("Unable to write url\n");
                    }
                });
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
