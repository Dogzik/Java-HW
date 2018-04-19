package ru.ifmo.rain.dovzhik.crawler;

import java.util.function.Predicate;

public class BookPredicate implements Predicate<String> {
    @Override
    public boolean test(String url) {
        return url.contains("e.lanbook.com/book/");
    }
}
