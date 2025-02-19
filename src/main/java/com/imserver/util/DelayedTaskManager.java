package com.imserver.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class DelayedTaskManager {
    private final ScheduledExecutorService scheduler;
    private final Map<String, ScheduledFuture<?>> taskMap = new ConcurrentHashMap<>(); // 存储任务 ID 和对应的 Future

    /**
     * 启动延时任务
     *
     * @param taskId   任务 ID
     * @param task     要执行的任务
     * @param delay    延时时间
     * @param timeUnit 时间单位
     */
    public void startDelayedTask(String taskId, Runnable task, long delay, TimeUnit timeUnit) {
        // 如果任务 ID 已存在，先取消旧任务
        if (taskMap.containsKey(taskId)) {
            cancelDelayedTask(taskId);
        }

        // 包装任务，使其在完成后自动清理
        Runnable wrappedTask = () -> {
            try {
                task.run(); // 执行任务
            } catch (Exception e) {
                log.error("任务 [{}] 执行发生异常。", taskId, e);
                throw e;
            } finally {
                taskMap.remove(taskId); // 任务完成后清理
                log.info("任务 [{}] 已完成并清理。", taskId);
            }
        };

        // 安排延时任务
        ScheduledFuture<?> future = scheduler.schedule(wrappedTask, delay, timeUnit);
        taskMap.put(taskId, future);
        log.info("任务 [{}] 已安排，将在 {} {} 后执行。", taskId, delay, timeUnit);
    }

    /**
     * 取消延时任务
     *
     * @param taskId 任务 ID
     */
    public void cancelDelayedTask(String taskId) {
        ScheduledFuture<?> future = taskMap.get(taskId);
        if (future != null && !future.isDone()) {
            future.cancel(false); // 取消任务
            taskMap.remove(taskId); // 从 Map 中移除任务
            log.info("任务 [{}] 已取消。", taskId);
        } else {
            log.info("任务 [{}] 不存在或已完成。", taskId);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DelayedTaskManager taskManager = new DelayedTaskManager(ThreadUtil.create(4));

        // 定义任务 1
        Runnable task1 = () -> log.info("任务 [task1] 执行了！");
        // 定义任务 2
        Runnable task2 = () -> log.info("任务 [task2] 执行了！");

        // 启动任务 1，5 秒后执行
        taskManager.startDelayedTask("task1", task1, 5, TimeUnit.SECONDS);
        // 启动任务 2，10 秒后执行
        taskManager.startDelayedTask("task2", task2, 10, TimeUnit.SECONDS);

        // 模拟等待 3 秒
        Thread.sleep(3000);

        // 取消任务 1
        taskManager.cancelDelayedTask("task1");

        // 模拟等待 10 秒，确保任务 2 执行
        Thread.sleep(10000);

        // 关闭调度器
        // taskManager.shutdown();
    }
}