package ru.ifmo.rain.dovzhik.crawler;

import java.util.Set;
import java.util.function.BiConsumer;

public class BookAdder implements BiConsumer<String, String> {
    private final Set<String> books;
    private final String INFO_BEGIN = "<div id=\"bibliographic_record\">";
    private final String INFO_END = "</div>";

    public BookAdder(final Set<String> storage) {
        books = storage;
    }

    private static boolean checkPage(final String clean) {
        if (clean.contains("fizika_0\"")
                || clean.contains("matematika_0\"")
                || clean.contains("informatika_0\"")) {
            for (int i = 2014; i <= 2018; i++) {
                if (clean.contains("<dt>Год:</dt><dd>" + Integer.toString(i) + "</dd>")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void accept(String url, String page) {
        page = page.replaceAll(">\\p{javaWhitespace}+<", "><");
        if (checkPage(page)) {
            final int beg = page.indexOf(INFO_BEGIN);
            final int end = page.indexOf(INFO_END, beg);
            books.add(page.substring(beg + INFO_BEGIN.length(), end).trim());
        }
    }
}
