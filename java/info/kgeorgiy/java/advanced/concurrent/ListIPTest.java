package info.kgeorgiy.java.advanced.concurrent;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ListIPTest extends ScalarIPTest<ListIP> {
    @Test
    public void test07_join() throws InterruptedException {
        test(
                (data, ignore) -> data.stream().map(Object::toString).collect(Collectors.joining()),
                (i, t, d, v) -> i.join(t, d),
                unit
        );
    }

    @Test
    public void test08_filter() throws InterruptedException {
        test(
                (data, predicate) -> data.stream().filter(predicate).collect(Collectors.toList()),
                ListIP::filter,
                predicates
        );
    }

    private final List<Named<Void>> unit = Arrays.asList(new Named<>("Common", null));
}
