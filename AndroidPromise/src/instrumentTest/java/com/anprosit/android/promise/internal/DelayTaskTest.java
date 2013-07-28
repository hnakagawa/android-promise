package com.anprosit.android.promise.internal;

import android.os.Bundle;
import android.test.AndroidTestCase;

/**
 * Created by Hirofumi Nakagawa on 13/07/15.
 */
public class DelayTaskTest extends AndroidTestCase {

	public void testDelayTask() {
		DelayTask<?, ?> task = new DelayTask<Object, Object>(100) {
			@Override
			public void run(Object value) {
			}

			@Override
			public void onFailed(Bundle value, Exception exp) {
			}
		};

		assertEquals(100, task.getDelay());
	}
}
