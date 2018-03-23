package info.kgeorgiy.java.advanced.concurrent;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface ListIP extends ScalarIP {
    String join(int threads, List<?> values) throws InterruptedException;

    <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException;

    <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException;
}
