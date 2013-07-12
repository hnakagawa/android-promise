package com.anprosit.android.promise.internal;

import android.test.AndroidTestCase;

/**
 * Created by Hirofumi Nakagawa on 13/07/15.
 */
public class DelayTaskTest extends AndroidTestCase {

    public void testDelayTask() {
        DelayTask<?, ?, ?> task = new DelayTask<Object, Object, Object>(100) {
            @Override
            public void run(Object value) {
            }

            @Override
            public void onFailed(Object value, Exception exp) {
            }
        };

        assertEquals(100, task.getDelay());
    }
}
