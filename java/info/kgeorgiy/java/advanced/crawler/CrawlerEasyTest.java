package info.kgeorgiy.java.advanced.crawler;

import info.kgeorgiy.java.advanced.base.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CrawlerEasyTest extends BaseTest {
    @Test
    public void test01_singlePage() throws IOException {
        test("http://en.ifmo.ru/en/page/50/Partnership.htm", 1);
    }

    @Test
    public void test02_pageAndLinks() throws IOException {
        test("http://www.ifmo.ru", 2);
    }

    @Test
    public void test03_deep() throws IOException {
        test("http://www.kgeorgiy.info", 4);
    }

    private void test(final String url, final int depth) throws IOException {
        test(url, depth, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 10, 10);
    }

    protected long test(final int downloaders, final int extractors, final int perHost, final int downloadTimeout, final int extractTimeout) throws IOException {
        return test("http://neerc.ifmo.ru/subregions/index.html", 3, downloaders, extractors, perHost, downloadTimeout, extractTimeout);
    }

    protected long test(final String url, final int depth, final int downloaders, final int extractors, final int perHost, final int downloadTimeout, final int extractTimeout) throws IOException {
        final ReplayDownloader replayDownloader = new ReplayDownloader(url, depth, downloadTimeout, extractTimeout);

        final long start = System.currentTimeMillis();
        final Result actual = download(url, depth, replayDownloader, downloaders, extractors, perHost);
        final long time = System.currentTimeMillis() - start;

        final Result expected = replayDownloader.expected(depth);
        checkResult(expected, actual);
        return time;
    }

    public static void checkResult(final Result expected, final Result actual) {
        compare("Downloaded OK", new HashSet<>(expected.getDownloaded()), new HashSet<>(actual.getDownloaded()));
        compare("Downloaded with errors", expected.getErrors().keySet(), actual.getErrors().keySet());
        Assert.assertEquals("Errors", expected.getErrors(), actual.getErrors());
    }

    private static void compare(final String context, final Set<String> expected, final Set<String> actual) {
        final Set<String> missing = difference(expected, actual);
        final Set<String> excess = difference(actual, expected);
        final String message = String.format("%s:%n    missing = %s%n    excess = %s%n", context, missing, excess);
        Assert.assertTrue(message, missing.isEmpty() && excess.isEmpty());
    }

    private static Result download(final String url, final int depth, final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        final CheckingDownloader checkingDownloader = new CheckingDownloader(downloader, downloaders, extractors, perHost);
        try (Crawler crawler = createInstance(checkingDownloader, downloaders, extractors, perHost)) {
            final Result result = crawler.download(url, depth);
            Assert.assertTrue(checkingDownloader.getError(), checkingDownloader.getError() == null);
            return result;
        }
    }

    private static Crawler createInstance(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        try {
            final Constructor<?> constructor = loadClass().getConstructor(Downloader.class, int.class, int.class, int.class);
            return (Crawler) constructor.newInstance(downloader, downloaders, extractors, perHost);
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    private static Set<String> difference(final Set<String> a, final Set<String> b) {
        return a.stream().filter(o -> !b.contains(o)).collect(Collectors.toSet());
    }
}
