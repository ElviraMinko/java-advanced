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

                tasksQueue.add(() -> {
                    try {
                        result.set(finalI, f.apply(element));
                    } catch (RuntimeException e) {
                        result.addException(e);
                    }
                });
                tasksQueue.notifyAll();
            }
        }
        if (result.checkIsAnyException()) {
            throw result.getException();
        }
        return result.getList();
    }

    @Override
    public void close() {
        for (Thread thread : threadList) {
            thread.interrupt();
        }
        boolean flag = false;
        for (int i = 0; i < threadList.size(); ) {
            try {
                threadList.get(i).join();
                i++;
            } catch (InterruptedException e) {
                flag = true;
            }
        }
        if (flag) {
            Thread.currentThread().interrupt();
        }
    }

    private static class MapperList<T> {
        List<T> list;
        int unmappedElements;
        RuntimeException exception = null;

        public MapperList(int size) {
            list = new ArrayList<>(Collections.nCopies(size, null));
            this.unmappedElements = size;
        }

        public synchronized void set(int index, T element) {
            list.set(index, element);
            unmappedElements--;
            this.notify();
        }

        public List<T> getList() {
            return list;
        }

        public synchronized void addException(RuntimeException e) {
            if (e != null) {
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
            this.notify();
        }

        public RuntimeException getException() {
            return exception;
        }


        public synchronized boolean checkIsAnyException() throws InterruptedException {
            while (unmappedElements > 0) {
                this.wait();
            }
            return exception != null;
        }

    }

}
