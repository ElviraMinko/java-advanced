package info.kgeorgiy.ja.minko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements an {@link ScalarIP} interface with parallel computing
 *
 * @author Elvira Minko
 */

public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper parallelMapper;

    /**
     * Default constructor
     */
    public IterativeParallelism() {
        parallelMapper = null;
    }

    /**
     * Constructor from {@link ParallelMapper}
     */
    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T> List<List<? extends T>> splitValues(int threads, List<? extends T> values) {
        List<List<? extends T>> returnValue = new ArrayList<>(threads);
        int block = values.size() / threads;
        int tail = values.size() % threads;
        threads = Math.min(threads, values.size());
        int start = 0;
        int end = block;
        for (int i = 0; i < threads; i++) {
            if (tail != 0) {
                tail--;
                end++;
            }
            returnValue.add(values.subList(start, end));
            start = end;
            end += block;
        }
        return returnValue;
    }

    private void finishThreadsWork(List<Thread> threadList, boolean isInterrupt, InterruptedException exception) {
        for (int i = 0; i < threadList.size(); i++) {
            try {
                threadList.get(i).join();
            } catch (InterruptedException e) {
                if (isInterrupt) {
                    for (int j = i; j < threadList.size(); j++) {
                        threadList.get(j).interrupt();
                    }
                }
                isInterrupt = false;
                exception.addSuppressed(e);
                finishThreadsWork(threadList.subList(i, threadList.size()), false, exception);
            }
        }
    }

    private <T, U> List<U> function(int threads, List<? extends T> values, Function<List<? extends T>, U> function) throws InterruptedException {
        var splitValues = splitValues(threads, values);
        List<U> result;
        if (parallelMapper == null) {
            result = new ArrayList<>(Collections.nCopies(splitValues.size(), null));
            List<Thread> threadList = new ArrayList<>(threads);
            for (int i = 0; i < splitValues.size(); i++) {
                int finalI = i;
                Thread thread = new Thread(() ->
                        result.set(finalI, function.apply(splitValues.get(finalI))));
                threadList.add(thread);
                thread.start();
            }

            boolean isInterrupt = true;
            InterruptedException exception = new InterruptedException();
            finishThreadsWork(threadList, isInterrupt, exception);
            if (!isInterrupt) {
                throw new InterruptedException("Interrupted exceptions: " + exception.getMessage());
            }
        } else {
            result = parallelMapper.map(function, splitValues);
        }
        return result;
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return function(threads, values, list -> list.stream().max(comparator).orElse(null)).stream().max(comparator).orElse(null);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return function(threads, values, list -> list.stream().allMatch(predicate)).stream().allMatch(it -> it);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return function(threads, values, list -> (int) list.stream().filter(predicate).count()).stream().mapToInt(Integer::intValue).sum();
    }
}
