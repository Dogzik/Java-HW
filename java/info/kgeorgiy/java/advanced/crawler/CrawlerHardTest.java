package info.kgeorgiy.java.advanced.crawler;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CrawlerHardTest extends CrawlerEasyTest {
    @Test
    public void test10_singleConnectionPerHost() throws IOException {
        test("http://www.ifmo.ru", 2, Integer.MAX_VALUE, Integer.MAX_VALUE, 1, 10, 10);
    }

    @Test
    public void test11_limitedConnectionsPerHost() throws IOException {
        test("http://www.ifmo.ru", 2, Integer.MAX_VALUE, Integer.MAX_VALUE, 10, 10, 10);
    }

    @Test
    public void test12_limitedConnectionsPerformance() throws IOException {
        final long time = test("http://www.ifmo.ru", 2, Integer.MAX_VALUE, Integer.MAX_VALUE, 3, 100, 100);
        System.out.println("Time: " + time);
        Assert.assertTrue("Too parallel", time > 2000);
        Assert.assertTrue("Not parallel", time < 4000);
    }
}
