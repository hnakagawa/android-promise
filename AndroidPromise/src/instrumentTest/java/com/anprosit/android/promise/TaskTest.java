package com.anprosit.android.promise;

import android.test.AndroidTestCase;

import com.anprosit.android.promise.test.MockPromiseContext;

/**
 * Created by Hirofumi Nakagawa on 13/07/13.
 */
public class TaskTest extends AndroidTestCase {

    private boolean mIsDone;

    private boolean mIsFailed;

    private Object mValue;

    private int mSeq;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mIsDone = false;
        mIsFailed = false;
        mValue = null;
        mSeq = 0;
    }

    public void testExecute() {
        Task<String, String, String> task = new Task<String, String, String>() {
            @Override
            public void run(String value) {
                assertEquals("aaa", value);
                next("bbb");
            }

            @Override
            public void onFailed(String value, Exception exp) {
            }
        };

        task.execute("aaa", new MockPromiseContext() {
            @Override
            public State getState() {
                return State.DOING;
            }

            @Override
            public void done(Object value) {
                mIsDone = true;
            }
        });
        assertTrue(mIsDone);
    }

    public void testExecuteWithFail() {
        Task<String, String, String> task = new Task<String, String, String>() {
            @Override
            public void run(String value) {
                fail("bbb");
            }

            @Override
            public void onFailed(String value, Exception exp) {
                mIsFailed = true;
                mValue = value;
            }
        };

        task.execute("aaa", new MockPromiseContext() {
            @Override
            public State getState() {
                return State.DOING;
            }

            @Override
            public void done(Object value) {
                mIsDone = true;
            }
        });

        assertTrue(mIsFailed);
        assertEquals("bbb", mValue);
    }

    public void testExecuteWithException() {
        final RuntimeException exp = new RuntimeException();
        Task<String, String, String> task = new Task<String, String, String>() {
            @Override
            public void run(String value) {
                throw exp;
            }

            @Override
            public void onFailed(String value, Exception exp) {
                mValue = exp;
            }
        };

        task.execute("aaa", new MockPromiseContext() {
            @Override
            public State getState() {
                return State.DOING;
            }

            @Override
            public void fail(Object value, Exception exp) {
                mIsFailed = true;
            }
        });

        assertTrue(mIsFailed);
        assertEquals(exp, mValue);
    }

    public void testExecuteWithIllegalState() {
        Task<String, String, String> task = new Task<String, String, String>() {
            @Override
            public void run(String value) {
                next(value);
            }

            @Override
            public void onFailed(String value, Exception exp) {
            }
        };

        task.execute("aaa", new MockPromiseContext() {
            @Override
            public State getState() {
                return State.DESTROYED;
            }

            @Override
            public void done(Object value) {
                mIsDone = true;
            }
        });
        assertFalse(mIsDone);
    }

    public void testNext() {
        Task<String, String, String> task = new Task<String, String, String>() {
            @Override
            public void run(String value) {
            }

            @Override
            public void onFailed(String value, Exception exp) {
            }
        };

        task.setContext(new MockPromiseContext() {
            @Override
            public State getState() {
                return State.DOING;
            }

            @Override
            public Task<?, ?, ?> getNextTask() {
                if (mSeq != 0)
                    return null;
                mSeq++;
                return new Task<Object, Object, Object>() {
                    @Override
                    public void run(Object value) {
                        mValue = (String) value;
                    }

                    @Override
                    public void onFailed(Object value, Exception exp) {
                    }
                };
            }
        });

        task.next("aaa");
        assertEquals("aaa", mValue);
    }

    public void testFail() {
        Task<String, String, String> task = new Task<String, String, String>() {
            @Override
            public void run(String value) {
            }

            @Override
            public void onFailed(String value, Exception exp) {
            }
        };

        task.setContext(new MockPromiseContext() {
            @Override
            public State getState() {
                return State.DOING;
            }

            @Override
            public void fail(Object value, Exception exception) {
                mValue = (String) value;
            }
        });

        task.fail("aaa", null);
        assertEquals("aaa", mValue);
    }
}
