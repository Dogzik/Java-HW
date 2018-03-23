package info.kgeorgiy.java.advanced.concurrent;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface ScalarIP {
    <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException;

    <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException;

    <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException;

    <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException;
}
