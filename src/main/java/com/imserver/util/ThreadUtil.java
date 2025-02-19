package com.imserver.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 一个基于虚拟线程的任务调度工具类，实现 ScheduledExecutorService 接口。
 */
public final class ThreadUtil implements ScheduledExecutorService {

    private final ScheduledExecutorService delegate;

    /**
     * 私有构造函数，初始化调度器。
     *
     * @param poolSize 调度线程池的大小。
     */
    private ThreadUtil(int poolSize) {
        this.delegate = Executors.newScheduledThreadPool(poolSize);
    }

    /**
     * 创建一个 VirtualThreadScheduler 实例。
     *
     * @param poolSize 调度线程池的大小。
     * @return VirtualThreadScheduler 实例。
     */
    public static ThreadUtil create(int poolSize) {
        return new ThreadUtil(poolSize);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return delegate.schedule(wrapWithVirtualThread(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return delegate.schedule(wrapWithVirtualThread(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return delegate.scheduleAtFixedRate(wrapWithVirtualThread(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return delegate.scheduleWithFixedDelay(wrapWithVirtualThread(command), initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
        List<StackWalker.StackFrame> stack = StackWalker.getInstance().walk(s -> s.limit(20).collect(Collectors.toList()));
        System.out.println("关闭了===================="+stack.stream().map(Object::toString).collect(Collectors.joining("\n")));
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(wrapWithVirtualThread(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(wrapWithVirtualThread(task), result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(wrapWithVirtualThread(task));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(wrapWithVirtualThread(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return delegate.invokeAll(wrapWithVirtualThread(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return delegate.invokeAny(wrapWithVirtualThread(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return delegate.invokeAny(wrapWithVirtualThread(tasks), timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(wrapWithVirtualThread(command));
    }

    /**
     * 将 Runnable 包装为在虚拟线程中执行的任务。
     */
    private Runnable wrapWithVirtualThread(Runnable task) {
        return () -> Thread.ofVirtual().start(task);
    }

    /**
     * 将 Callable 包装为在虚拟线程中执行的任务。
     */
    private <V> Callable<V> wrapWithVirtualThread(Callable<V> task) {
        return () -> {
            FutureTask<V> futureTask = new FutureTask<>(task);
            Thread.ofVirtual().start(futureTask);
            return futureTask.get();
        };
    }

    /**
     * 将 Callable 集合包装为在虚拟线程中执行的任务集合。
     */
    private <T> Collection<Callable<T>> wrapWithVirtualThread(Collection<? extends Callable<T>> tasks) {
        return tasks.stream()
                .map(this::wrapWithVirtualThread)
                .toList();
    }
}