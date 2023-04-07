package info.kgeorgiy.ja.minko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.ArrayList;
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

    private <T> List<List<? extends T>> splitValues(int threads, List<? extends T> values) {
        List<List<? extends T>> returnValue = new ArrayList<>(threads);
        int block = values.size() / threads;
        int tail = values.size() % threads;
        int[] blocks = new int[threads];
        for (int i = 0; i < threads; i++) {
            blocks[i] = block;
            if (tail != 0) {
                blocks[i]++;
                tail--;
            }
        }
        int prefixSize = 0;
        for (int i = 0; i < threads; i++) {
            if (blocks[i] > 0) {
                returnValue.add(values.subList(prefixSize, prefixSize + blocks[i]));
            }
            prefixSize += blocks[i];
        }
        return returnValue;
    }


    private <T, U> List<U> function(int threads, List<? extends T> values, Function<List<? extends T>, U> function) throws InterruptedException {
        var splitValues = splitValues(threads, values);

        List<U> result = new ArrayList<>(threads);
        for (int i = 0; i < splitValues.size(); i++) {
            result.add(null);
        }
        List<Thread> threadList = new ArrayList<>(threads);
        for (int i = 0; i < splitValues.size(); i++) {
            int finalI = i;
            Thread thread = new Thread(() ->
                    result.set(finalI, function.apply(splitValues.get(finalI))));
            threadList.add(thread);
            thread.start();
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                for (Thread value : threadList) {
                    value.interrupt();
                }
                throw new InterruptedException(e.getMessage());
            }
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
