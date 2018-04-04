package ru.ifmo.rain.dovzhik.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

import static ru.ifmo.rain.dovzhik.concurrent.ConcurrentUtils.addAndStart;
import static ru.ifmo.rain.dovzhik.concurrent.ConcurrentUtils.checkThread;
import static ru.ifmo.rain.dovzhik.concurrent.ConcurrentUtils.joinThreadsUninterruptedly;

public class ParallelMapperImpl implements ParallelMapper {
    private final Queue<Runnable> tasks;
    private final List<Thread> workers;
    private final static int MAX_SIZE = 1_000_000;

    private void solveTask() throws InterruptedException {
        Runnable task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
            tasks.notifyAll();
        }
        task.run();
    }

    private void addTask(final Runnable task) throws InterruptedException {
        synchronized (tasks) {
            while (tasks.size() == MAX_SIZE) {
                tasks.wait();
            }
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    private class ResultCollector<R> {
        private final List<R> res;
        private int cnt;

        ResultCollector(final int size) {
            res = new ArrayList<>(Collections.nCopies(size, null));
            cnt = 0;
        }

        void setData(final int pos, R data) {
            res.set(pos, data);
            synchronized (this) {
                if (++cnt == res.size()) {
                    notify();
                }
            }
        }

        synchronized List<R> getRes() throws InterruptedException {
            while (cnt < res.size()) {
                wait();
            }
            return res;
        }
    }

    public ParallelMapperImpl(final int threads) {
        checkThread(threads);
        tasks = new ArrayDeque<>();
        workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            addAndStart(workers, new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        solveTask();
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        ResultCollector<R> collector = new ResultCollector<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int ind = i;
            addTask(() -> collector.setData(ind, f.apply(args.get(ind))));
        }
        return collector.getRes();
    }

    @Override
    public void close() {
        workers.forEach(Thread::interrupt);
        joinThreadsUninterruptedly(workers);
    }
}
