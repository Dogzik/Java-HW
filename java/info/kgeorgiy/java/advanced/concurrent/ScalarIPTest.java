package info.kgeorgiy.java.advanced.concurrent;

import info.kgeorgiy.java.advanced.base.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScalarIPTest<P extends ScalarIP> extends BaseTest {
    public static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    public static final Comparator<Integer> BURN_COMPARATOR = (o1, o2) -> {
        int total = o1 + o2;
        for (int i = 0; i < 5_000_000; i++) {
            total += i;
        }
        if (total == o1 + o2) {
            throw new AssertionError();
        }
        return Integer.compare(o1, o2);
    };

    public static final Comparator<Integer> SLEEP_COMPARATOR = (o1, o2) -> {
        try {
            Thread.sleep(10);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        return Integer.compare(o1, o2);
    };

    public static final List<Integer> sizes = Arrays.asList(10_000, 5, 2, 1);
    private final Random random = new Random(3257083275083275083L);
    protected List<Integer> factors = Collections.singletonList(0);

    @Test
    public void test01_maximum() throws InterruptedException {
        test(Collections::max, ScalarIP::maximum, comparators);
    }

    @Test
    public void test02_minimum() throws InterruptedException {
        test(Collections::min, ScalarIP::minimum, comparators);
    }

    @Test
    public void test03_all() throws InterruptedException {
        test((data, predicate) -> data.stream().allMatch(predicate), ScalarIP::all, predicates);
    }

    @Test
    public void test04_any() throws InterruptedException {
        test((data, predicate) -> data.stream().anyMatch(predicate), ScalarIP::any, predicates);
    }

    @Test
    public void test05_sleepPerformance() throws InterruptedException {
        final List<Integer> data = randomList(100 * PROCESSORS);
        final double speedup = speedup(data, SLEEP_COMPARATOR, PROCESSORS * 2);
        Assert.assertTrue("Not parallel", speedup > 0.66);
    }

    @Test
    public void test06_burnPerformance() throws InterruptedException {
        final List<Integer> data = randomList(100 * PROCESSORS);
        final double speedup = speedup(data, BURN_COMPARATOR, PROCESSORS);
        Assert.assertTrue("Not parallel", speedup > 0.66);
        Assert.assertTrue("Too parallel", speedup < 1.5);
    }

    protected double speedup(final List<Integer> data, final Comparator<Integer> comparator, final int threads) throws InterruptedException {
        System.err.println("    Warm up");
        final ConcurrentFunction<P, Integer, Comparator<Integer>> maximum = ScalarIP::maximum;
        for (int i = 0; i < 5; i++) {
            performance(threads, threads, data, maximum, comparator);
        }
        System.err.println("    Measurement");

        final double performance1 = performance(1, threads, data, maximum, comparator);
        final double performance2 = performance(threads, threads, data, maximum, comparator);
        final double speedup = performance2 / performance1;
        System.err.format("    Performance ratio %.1f for %d threads (%.1f %.1f ms/op)%n", speedup, threads, performance1, performance2);
        return speedup;
    }

    protected int getSubtasks(final int threads, final int totalThreads) {
        return threads;
    }

   private double performance(final int threads, final int totalThreads, final List<Integer> data, final ConcurrentFunction<P, Integer, Comparator<Integer>> f, final Comparator<Integer> comparator) throws InterruptedException {
        final int subtasks = getSubtasks(threads, totalThreads);
        final long start = System.nanoTime();
        f.apply(createInstance(threads), subtasks, data, comparator);
        final long time = System.nanoTime() - start;

        final double ops = (subtasks - 1) + (Math.ceil(data.size() / (double) threads) - 1);
        return time / 1e6 / ops;
    }

    protected <T, U> void test(final BiFunction<List<Integer>, U, T> fExpected, final ConcurrentFunction<P, T, U> fActual, final List<Named<U>> cases) throws InterruptedException {
        for (final int factor : factors) {
            final P instance = createInstance(factor);
            for (final int n : sizes) {
                System.err.println("    --- Size " + n);
                final List<Integer> data = randomList(n);
                for (final Named<U> named : cases) {
                    final T expected = fExpected.apply(data, named.value);
                    System.err.print("        " + named.name + ", threads: ");
                    for (int threads = 1; threads <= 10; threads++) {
                        System.err.print(" " + threads);
                        Assert.assertEquals(threads + " threads", expected, fActual.apply(instance, threads, data, named.value));
                    }
                    System.err.println();
                }
                System.err.println();
            }
        }
    }

    interface ConcurrentFunction<P, T, U> {
        T apply(P instance, int threads, List<Integer> data, U value) throws InterruptedException;
    }

    protected List<Integer> randomList(final int size) {
        final int[] pool = random.ints(Math.min(size, 1000_000)).toArray();
        return IntStream.generate(() -> pool[random.nextInt(pool.length)]).limit(size).boxed().collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    protected P createInstance(final int threads) {
        return createCUT();
    }

    protected final class Named<T> {
        public final String name;
        public final T value;

        public Named(final String name, final T value) {
            this.name = name;
            this.value = value;
        }
    }

    protected final List<Named<Comparator<Integer>>> comparators = Arrays.asList(
            new Named<>("Natural order", Integer::compare),
            new Named<>("Reverse order", (l1, l2) -> Integer.compare(l2, l1)),
            new Named<>("Div 100", Comparator.<Integer>comparingInt(v -> v / 100)),
            new Named<>("Even first", Comparator.<Integer>comparingInt(v -> v % 2).thenComparing(v -> v)),
            new Named<>("All equal", (v1, v2) -> 0)
    );

    protected final List<Named<Predicate<Integer>>> predicates = Arrays.asList(
            new Named<>("Equal 0", Predicate.isEqual(0)),
            new Named<>("Greater than 0", i -> i > 0),
            new Named<>("Even", i -> i % 2 == 0),
            new Named<>("True", i -> true),
            new Named<>("False", i -> false)
    );
}
