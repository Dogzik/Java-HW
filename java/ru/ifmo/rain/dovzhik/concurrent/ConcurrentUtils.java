package ru.ifmo.rain.dovzhik.concurrent;

import java.util.List;

class ConcurrentUtils {
    static void addAndStart(List<Thread> workers, Thread thread) {
        workers.add(thread);
        thread.start();
    }

    static void joinThreads(final List<Thread> threads) throws InterruptedException {
        InterruptedException exception = null;
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                if (exception == null) {
                    exception = new InterruptedException("Not all threads joined");
                }
                exception.addSuppressed(e);
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    static void joinThreadsUninterruptedly(final List<Thread> threads) {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {}
        }
    }
}
