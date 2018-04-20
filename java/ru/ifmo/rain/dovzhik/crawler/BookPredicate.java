package ru.ifmo.rain.dovzhik.crawler;

import java.util.function.Predicate;

public class BookPredicate implements Predicate<String> {
    @Override
    public boolean test(String url) {
        return url.contains("e.lanbook.com/book/")
                || url.contains("e.lanbook.com/books/917")
                || url.contains("e.lanbook.com/books/918")
                || url.contains("e.lanbook.com/books/1537")
                || url.endsWith("e.lanbook.com/books");
    }
}
