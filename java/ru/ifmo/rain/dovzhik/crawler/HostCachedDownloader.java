package ru.ifmo.rain.dovzhik.crawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.SequenceInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HostCachedDownloader implements Downloader {
    private final String host;
    private final Predicate<String> downloadChecker;
    private final Predicate<String> processChecker;
    private final BiConsumer<String, String> pageHandler;

    private static final byte[] OK_MARKER = {'+'};
    private static final byte[] FAIL_MARKER = {'-'};

    private final Path directory;

    HostCachedDownloader(final String dir, final String host,
                         final Predicate<String> dc, Predicate<String> pc,
                         BiConsumer<String, String> pageHandler) throws IOException {
        this.host = host;
        directory = Paths.get(dir);
        downloadChecker = dc;
        processChecker = pc;
        this.pageHandler = pageHandler;
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        if (!Files.isDirectory(directory)) {
            throw new IOException(directory + " is not a directory");
        }
    }


    private boolean downloadablePage(final String url) {
        try {
            return URLUtils.getHost(url).equals(host) && downloadChecker.test(url);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static String normalize(final String url) {
        final int barrier = url.indexOf('?');
        if (barrier == -1) {
            return url;
        }
        final String core = url.substring(0, barrier);
        final String[] flags = url.substring(barrier + 1).split("&");
        for (final String flag : flags) {
            if (flag.matches("page=\\d+")) {
                return core + "?" + flag;
            }
        }
        return core;
    }

    @Override
    public Document download(String url) throws IOException {
        if (!downloadablePage(url)) {
            throw new IOException("Wrong URL");
        }
        final URI uri = URLUtils.getURI(url);
        final Path file = directory.resolve(URLEncoder.encode(uri.toString(), "UTF-8"));
        if (Files.notExists(file)) {
            System.out.println("Downloading " + url);
            try {
                try (final InputStream is = uri.toURL().openStream()) {
                    Files.copy(new SequenceInputStream(new ByteArrayInputStream(OK_MARKER), is), file);
                }
            } catch (final IOException e) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(FAIL_MARKER);
                try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
                    oos.writeObject(e);
                }
                Files.copy(new ByteArrayInputStream(out.toByteArray()), file);
                throw e;
            }
            System.out.println("Downloaded " + uri);
        } else {
            System.out.println("Already downloaded " + url);
            try (final InputStream is = Files.newInputStream(file)) {
                if (is.read() == FAIL_MARKER[0]) {
                    try (ObjectInputStream ois = new ObjectInputStream(is)) {
                        throw (IOException) ois.readObject();
                    } catch (final ClassNotFoundException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }

        if (processChecker.test(url)) {
            try (final BufferedReader is = Files.newBufferedReader(file)){
                if (!(is.read() == FAIL_MARKER[0])) {
                    pageHandler.accept(url, is.lines().collect(Collectors.joining()));
                }
            }
        }


        return () -> {
            try (final InputStream is = Files.newInputStream(file)) {
                return is.read() == FAIL_MARKER[0] ? Collections.emptyList()
                        : URLUtils.extractLinks(uri, is).stream()
                        .filter(this::downloadablePage)
                        .map(HostCachedDownloader::normalize)
                        .collect(Collectors.toList());
            }
        };
    }
}
