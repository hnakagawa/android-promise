package com.anprosit.android.promise.internal;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Hirofumi Nakagawa on 13/07/14.
 */
public class AsyncThreadTask<T, V, E> extends DelayTask<T, V, E> {
    private static final String TAG = AsyncThreadTask.class.getSimpleName();

    private static final int CORE_POOL_SIZE = 3;

    private static final int MAXIMUM_POOL_SIZE = 64;

    private static final int KEEP_ALIVE = 1;

    private static final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, TAG + " #" + mCount.getAndIncrement());
        }
    };

    private static final Executor sThreadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

    private final Executor mExecutor;

    public AsyncThreadTask(Executor executor, long delay) {
        super(delay);
        mExecutor = executor;
    }

    public AsyncThreadTask(long delay) {
        this(sThreadPoolExecutor, delay);
    }

    @Override
    public void execute(final T value, final PromiseContext context) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (context.getState() != PromiseContext.State.DOING)
                        return;
                    setContext(context);
                    Thread.sleep(getDelay());
                    AsyncThreadTask.this.run(value);
                } catch (Exception exp) {
                    fail(null, exp);
                }
            }
        });
    }

    @Override
    public void run(T value) {
        next((V) value);
    }

    @Override
    public void onFailed(E value, Exception exp) {
        Log.w(TAG, exp.getMessage() + "", exp);
    }
}
