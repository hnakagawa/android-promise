package com.anprosit.android.promise.internal;

import com.anprosit.android.promise.Task;

/**
 * Created by Hirofumi Nakagawa on 13/07/14.
 */
public abstract class DelayTask<T, V, E> extends Task<T, V, E> {
    private final long mDelay;

    public DelayTask(long delay) {
        mDelay = delay;
    }

    public long getDelay() {
        return mDelay;
    }
}
