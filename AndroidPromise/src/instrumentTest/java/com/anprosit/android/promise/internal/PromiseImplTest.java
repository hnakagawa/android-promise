package com.anprosit.android.promise.internal;

import android.os.Bundle;
import android.test.AndroidTestCase;

import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;

import java.util.Collection;

/**
 * Created by Hirofumi Nakagawa on 13/07/15.
 */
public class PromiseImplTest extends AndroidTestCase {
    private PromiseImpl<String, String> mPromise;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mPromise = (PromiseImpl<String, String>) Promise.newInstance(getContext(), String.class);
    }

    @Override
    public void tearDown() throws Exception {
        Promise.destroy(getContext());
        super.tearDown();
    }

    public void testThenWithTask() {
        mPromise.then(new Task<String, String>() {
            @Override
            public void run(String value) {
            }

            @Override
            public void onFailed(Bundle value, Exception exp) {
            }
        });

        Collection<Task<?, ?>> tasks = mPromise.anatomy();
        assertEquals(1, tasks.size());
        assertEquals(PromiseContext.State.DESTROYED, mPromise.getState());
    }

    public void testThenOnMainThread() {
        mPromise.thenOnMainThread(new Task<String, String>() {
            @Override
            public void run(String value) {
            }

            @Override
            public void onFailed(Bundle value, Exception exp) {
            }
        });

        Collection<Task<?, ?>> tasks = mPromise.anatomy();
        assertEquals(2, tasks.size());
        assertEquals(PromiseContext.State.DESTROYED, mPromise.getState());
    }

    public void testThenOnAsyncThread() {
        mPromise.thenOnAsyncThread(new Task<String, String>() {
            @Override
            public void run(String value) {
            }

            @Override
            public void onFailed(Bundle value, Exception exp) {
            }
        });

        Collection<Task<?, ?>> tasks = mPromise.anatomy();
        assertEquals(2, tasks.size());
        assertEquals(PromiseContext.State.DESTROYED, mPromise.getState());
    }

    public void testIsCompleted() {
        assertFalse(mPromise.isCompleted());
        mPromise.mState = PromiseContext.State.DOING;
        mPromise.done(null);
        assertTrue(mPromise.isCompleted());
    }

    public void testCancel() {
        assertEquals(PromiseContext.State.READY, mPromise.getState());
        mPromise.mState = PromiseContext.State.DOING;
        mPromise.cancel();
        assertEquals(PromiseContext.State.CANCELLED, mPromise.getState());
    }

    public void testFail() {
        assertEquals(PromiseContext.State.READY, mPromise.getState());
        mPromise.mState = PromiseContext.State.DOING;
        mPromise.fail(null, new Exception());
        assertEquals(PromiseContext.State.FAILED, mPromise.getState());
    }

    public void testGetNextTask() {
        mPromise.then(new Task<String, String>() {
            @Override
            public void run(String value) {
            }

            @Override
            public void onFailed(Bundle value, Exception exp) {
            }
        });

        assertNotNull(mPromise.getNextTask());
        assertNull(mPromise.getNextTask());
    }

    public void testExecute() {
        mPromise.then(new Task<String, String>() {
            @Override
            public void run(String value) {
                next(value);
            }

            @Override
            public void onFailed(Bundle value, Exception exp) {
            }
        });

        assertEquals(PromiseContext.State.READY, mPromise.getState());
        mPromise.execute("aaa", null);
        assertEquals(PromiseContext.State.DONE, mPromise.getState());
    }

    public void testExecuteWithEmptyTask() {
        assertEquals(PromiseContext.State.READY, mPromise.getState());
        mPromise.execute("aaa", null);
        assertEquals(PromiseContext.State.DONE, mPromise.getState());
    }
}
