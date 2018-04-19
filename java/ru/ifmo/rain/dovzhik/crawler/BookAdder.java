package ru.ifmo.rain.dovzhik.crawler;

import java.util.Set;
import java.util.function.BiConsumer;

public class BookAdder implements BiConsumer<String, String> {
    private final Set<String> books;

    public BookAdder(final Set<String> storage) {
        books = storage;
    }

    private static boolean checkPage(final String page) {
        final String clean = page.replaceAll("\\p{javaWhitespace}+", "");
        if (clean.contains("fizika_0\">Физика<")
                || clean.contains("matematika_0\">Математика<")
                || clean.contains("informatika_0\">Информатика<")) {
            for (int i = 2013; i <= 2018; i++) {
                if (clean.contains("<dt>Год:</dt><dd>" + Integer.toString(i) + "</dd>")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void accept(String url, String page) {
        if (checkPage(page)) {
            books.add(url);
        }
    }
}
