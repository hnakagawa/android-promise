package com.anprosit.android.promise.test;

import com.anprosit.android.promise.Task;
import com.anprosit.android.promise.internal.PromiseContext;

/**
 * Created by Hirofumi Nakagawa on 13/07/15.
 */
public class MockPromiseContext implements PromiseContext {
    @Override
    public State getState() {
        return null;
    }

    @Override
    public void done(Object value) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public void fail(Object result, Exception exception) {

    }

    @Override
    public Task<?, ?, ?> getNextTask() {
        return null;
    }
}
