package ru.ifmo.rain.dovzhik.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IterativeParallelism implements ListIP {
    private static void joinThreads(final List<Thread> threads) throws InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private static void startThreads(final List<Thread> threads) {
        for (Thread thread : threads) {
            thread.start();
        }
    }

    private static <T, R> R baseTask(int threads, final List<? extends T> values,
                                     final Function<? super List<? extends T>, ? extends R> task,
                                     final Function<? super List<? extends R>, ? extends R> ansCollector)
            throws InterruptedException {
        if (threads <= 0) {
            throw new IllegalArgumentException("Number of threads must be positive");
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Unable to handle empty list");
        }
        threads = Math.min(threads, values.size());
        final List<Thread> workers = new ArrayList<>(Collections.nCopies(threads, null));
        final List<R> res = new ArrayList<>(Collections.nCopies(threads, null));
        final int blockSize = values.size() / threads;
        for (int i = 0; i < threads; i++) {
            final int l = i * blockSize;
            final int r = (i == threads - 1) ? values.size() : (i + 1) * blockSize;
            final int pos = i;
            workers.set(i, new Thread(() -> res.set(pos, task.apply(values.subList(l, r)))));
        }
        startThreads(workers);
        joinThreads(workers);
        return ansCollector.apply(res);
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        final Function<List<? extends T>, ? extends T> listMax = list -> Collections.max(list, comparator);
        return baseTask(threads, values, listMax, listMax);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return baseTask(threads, values,
                list -> list.stream().allMatch(predicate),
                list -> list.stream().allMatch(elem -> elem));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, elem -> !predicate.test(elem));
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return baseTask(threads, values,
                list -> list.stream().map(Object::toString).collect(Collectors.joining()),
                list -> list.stream().collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return baseTask(threads, values,
                list -> list.stream().filter(predicate).collect(Collectors.toList()),
                list -> list.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return baseTask(threads, values,
                list -> list.stream().map(f).collect(Collectors.toList()),
                list -> list.stream().flatMap(Collection::stream).collect(Collectors.toList()));
    }
}
