package com.imserver.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 锁工具类
 */
public class LockUtil {

    /**
     * 存储 key 对应 锁 的弱引用
     */
    private static final ConcurrentHashMap<String, LockWeakReference> LOCK_MAP = new ConcurrentHashMap<>();

    /**
     * 存储已过期的 ref
     */
    private static final ReferenceQueue<Lock> REFERENCE_QUEUE = new ReferenceQueue<>();

    /**
     * 获取 key 对应的 lock
     *
     * @param lockKey 锁KEY
     * @return 锁对象
     */
    public static Lock getLock(String lockKey) {
        // 清理过时数据
        expungeStaleEntries();

        // 获取 key 对应的 lock 弱引用
        LockWeakReference weakReference = LOCK_MAP.get(lockKey);

        // 获取lock
        Lock lock = (weakReference == null ? null : weakReference.get());

        // 这里使用 while 循环获取，防止在获取过程中lock被gc回收
        while (lock == null) {
            // 使用 computeIfAbsent，在多线程环境下，针对同一 key ，weakReference 均指向同一弱引用对象
            // 这里使用 可重入锁
            weakReference = LOCK_MAP.computeIfAbsent(lockKey, key -> new LockWeakReference(key, new ReentrantLock(), REFERENCE_QUEUE));

            // 获取弱引用指向的lock，这里如果获取到 lock 对象值，将会使 lock 对象值的弱引用提升为强引用，不会被gc回收
            lock = weakReference.get();

            // 在 computeIfAbsent 的执行和 weakReference.get() 执行的间隙，可能存在执行gc的过程，会导致 lock 为null，所以使用while循环获取
            if (lock != null) {
                return lock;
            }

            // 获取不到 lock，移除map中无用的ref
            expungeStaleEntries();
        }
        return lock;
    }

    /**
     * 锁
     *
     * @param lockKeyPrefix 锁KEY前缀
     * @param args          参数
     * @return 锁
     */
    public static Lock getLock(String lockKeyPrefix, Object... args) {
        Objects.requireNonNull(lockKeyPrefix, "锁前缀不可为空");
        Objects.requireNonNull(args, "锁参数不可为空");
        return getLock(lockKeyPrefix + Stream.of(args).map(arg -> arg == null
                ? "" : String.valueOf(arg)).collect(Collectors.joining("_")));
    }

    /**
     * 清除 map 中已被回收的 ref
     */
    private static void expungeStaleEntries() {
        Reference<? extends Lock> ref;
        while ((ref = REFERENCE_QUEUE.poll()) != null) {
            LockWeakReference lockWeakReference = (LockWeakReference) ref;
            LOCK_MAP.remove(lockWeakReference.lockKey);
        }
    }

    static class LockWeakReference extends WeakReference<Lock> {
        /**
         * 存储 弱引用 对应的 key 值，方便 之后的 remove 操作
         */
        private String lockKey;

        public LockWeakReference(String key, ReentrantLock lock, ReferenceQueue<? super Lock> referenceQueue) {
            super(lock, referenceQueue);
            this.lockKey = key;
        }
    }
}
