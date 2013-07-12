package com.anprosit.android.promise.internal;

import android.os.Handler;

import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.ResultCallback;
import com.anprosit.android.promise.Task;
import com.anprosit.android.promise.internal.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Hirofumi Nakagawa on 13/07/12.
 */
public class PromiseImpl extends Promise implements PromiseContext {
    private List<Task<?, ?, ?>> mTasks = new ArrayList<Task<?, ?, ?>>();

    private final Handler mHandler;

    private int mIndex;

    private ResultCallback<?, ?> mResultCallback;

    private CountDownLatch mLatch = new CountDownLatch(1);

    protected State mState = State.READY;

    public PromiseImpl(Handler handler) {
        mHandler = handler;
    }

    @Override
    public Promise then(Task<?, ?, ?> task) {
        synchronized (this) {
            addTask(task);
        }
        return this;
    }

    @Override
    public Promise then(Promise promise) {
        synchronized (this) {
            addTasks(promise.anatomy());
        }
        return this;
    }

    @Override
    public Promise thenOnMainThread(Task<?, ?, ?> task) {
        return thenOnMainThread(task, 0);
    }

    @Override
    public Promise thenOnMainThread(Task<?, ?, ?> task, long delay) {
        synchronized (this) {
            addTask(new HandlerThreadTask(delay));
            addTask(task);
        }
        return this;
    }

    @Override
    public Promise thenOnAsyncThread(Task<?, ?, ?> task) {
        return thenOnAsyncThread(task, 0);
    }

    @Override
    public Promise thenOnAsyncThread(Task<?, ?, ?> task, long delay) {
        synchronized (this) {
            addTask(new AsyncThreadTask(delay));
            addTask(task);
        }
        return this;
    }

    @Override
    public synchronized void execute(Object value, ResultCallback<?, ?> resultCallback) {
        if (getState() != State.READY)
            throw new IllegalStateException("Promise#execute method must be called in READY state");

        mResultCallback = resultCallback;

        Task<Object, ?, ?> next = (Task<Object, ?, ?>) getNextTask();

        mState = State.DOING;

        if (next == null) {
            done(value);
            return;
        }

        next.execute(value, this);
    }

    @Override
    public synchronized Collection<Task<?, ?, ?>> anatomy() {
        if (mState != State.READY)
            throw new IllegalStateException("Promise#anatomy method must be called in READY state");
        mState = State.DESTROYED;
        mLatch.countDown();
        return mTasks;
    }

    @Override
    public synchronized void done(final Object result) {
        if (mState != State.DOING)
            return;

        mState = State.DONE;

        final ResultCallback<Object, ?> callback = (ResultCallback<Object, ?>) mResultCallback;
        if (callback == null)
            return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (callback != null)
                        callback.onCompleted(result);
                } finally {
                    mLatch.countDown();
                }
            }
        });
    }

    @Override
    public synchronized boolean isCompleted() {
        return mState == State.DONE;
    }

    @Override
    public synchronized boolean isFailed() {
        return mState == State.FAILED;
    }

    @Override
    public synchronized void cancel() {
        if (mState != State.DOING)
            return;
        mState = State.CANCELLED;
        mLatch.countDown();
    }

    @Override
    public synchronized boolean isCancelled() {
        return mState == State.CANCELLED;
    }

    @Override
    public void await() {
        ThreadUtils.checkNotMainThread();
        try {
            mLatch.await();
        } catch (InterruptedException exp) {
        }
    }

    @Override
    public synchronized void fail(final Object result, final Exception exception) {
        if (mState != State.DOING)
            return;

        mState = State.FAILED;

        final ResultCallback<?, Object> callback = (ResultCallback<?, Object>) mResultCallback;
        if (callback == null)
            return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (callback != null)
                        callback.onFailed(result, exception);
                } finally {
                    mLatch.countDown();
                }
            }
        });
    }

    @Override
    public synchronized State getState() {
        return mState;
    }

    @Override
    public synchronized Task<?, ?, ?> getNextTask() {
        if (mIndex >= mTasks.size())
            return null;
        return mTasks.get(mIndex++);
    }

    private void addTask(Task<?, ?, ?> task) {
        mTasks.add(task);
    }

    private void addTasks(Collection<Task<?, ?, ?>> tasks) {
        mTasks.addAll(tasks);
    }
}
