package info.kgeorgiy.ja.minko.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

/**
 * Implements an {@link ParallelMapper} interface
 *
 * @author Elvira Minko
 */
public class ParallelMapperImpl implements ParallelMapper {

    private final List<Thread> threadList = new ArrayList<>();

    private final Deque<Runnable> tasksQueue = new ArrayDeque<>();

    public ParallelMapperImpl(int threads) {
        for (int i = 0; i < threads; i++) {
            threadList.add(new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
                        synchronized (tasksQueue) {
                            while (tasksQueue.isEmpty()) {
                                tasksQueue.wait();
                            }
                            task = tasksQueue.remove();
                        }
                        task.run();
                    }
                } catch (InterruptedException e) {
                    //
                } finally {
                    Thread.currentThread().interrupt();
                }
            }));
            threadList.get(i).start();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        MapperList<R> result = new MapperList<>(args.size());

        for (int i = 0; i < args.size(); i++) {
            synchronized (tasksQueue) {
                final T element = args.get(i);
                int finalI = i;
                tasksQueue.add(() -> result.set(finalI, f.apply(element)));
                tasksQueue.notifyAll();
            }
        }
        return result.getList();
    }

    @Override
    public void close() {
        for (Thread thread : threadList) {
            thread.interrupt();
        }
        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread has interrupted the current thread" + e.getMessage());
            }
        }
    }

    private static class MapperList<T> {
        List<T> list;
        int unmappedElements;

        public MapperList(int size) {
            list = new ArrayList<>(Collections.nCopies(size, null));
            this.unmappedElements = size;
        }

        public synchronized void set(int index, T element) {
            list.set(index, element);
            unmappedElements--;
            this.notify();
        }

        public synchronized List<T> getList() throws InterruptedException {
            while (unmappedElements > 0) {
                this.wait();
            }
            return list;
        }
    }

}
