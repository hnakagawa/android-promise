package com.anprosit.android.promise;

import android.os.Bundle;
import android.test.AndroidTestCase;

import com.anprosit.android.promise.test.MockPromiseContext;

/**
 * Created by Hirofumi Nakagawa on 13/07/13.
 */
public class TaskTest extends AndroidTestCase {

	private boolean mIsDone;

	private boolean mIsFailed;

	private Object mValue;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		mIsDone = false;
		mIsFailed = false;
		mValue = null;
	}

	public void testExecute() {
		Task<String, String> task = new Task<String, String>() {
			@Override
			public void run(String value) {
				assertEquals("aaa", value);
				next("bbb");
			}

			@Override
			public void onFailed(Bundle value, Exception exp) {
			}
		};

		task.execute("aaa", new MockPromiseContext() {
			@Override
			public State getState() {
				return State.ALIVE;
			}

			@Override
			public void done(Object value) {
				mIsDone = true;
			}
		}, 0);
		assertTrue(mIsDone);
	}

	public void testExecuteWithFail() {
		final Bundle bundle = new Bundle();
		Task<String, String> task = new Task<String, String>() {
			@Override
			public void run(String value) {
				fail(bundle);
			}

			@Override
			public void onFailed(Bundle value, Exception exp) {
				mIsFailed = true;
				mValue = value;
			}
		};

		task.execute("aaa", new MockPromiseContext() {
			@Override
			public State getState() {
				return State.ALIVE;
			}

			@Override
			public void done(Object value) {
				mIsDone = true;
			}
		}, 0);

		assertTrue(mIsFailed);
		assertEquals(bundle, mValue);
	}

	public void testExecuteWithException() {
		final RuntimeException exp = new RuntimeException();
		Task<String, String> task = new Task<String, String>() {
			@Override
			public void run(String value) {
				throw exp;
			}

			@Override
			public void onFailed(Bundle value, Exception exp) {
				mValue = exp;
			}
		};

		task.execute("aaa", new MockPromiseContext() {
			@Override
			public State getState() {
				return State.ALIVE;
			}

			@Override
			public void fail(Bundle value, Exception exp) {
				mIsFailed = true;
			}
		}, 0);

		assertTrue(mIsFailed);
		assertEquals(exp, mValue);
	}

	public void testExecuteWithIllegalState() {
		Task<String, String> task = new Task<String, String>() {
			@Override
			public void run(String value) {
				next(value);
			}

			@Override
			public void onFailed(Bundle value, Exception exp) {
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
		}, 0);
		assertFalse(mIsDone);
	}

	public void testNext() {
		Task<String, String> task = new Task<String, String>() {
			@Override
			public void run(String value) {
			}

			@Override
			public void onFailed(Bundle value, Exception exp) {
			}
		};

		task.mPromiseContext = new MockPromiseContext() {
			@Override
			public State getState() {
				return State.ALIVE;
			}

			@Override
			public Task<?, ?> getTask(int index) {
				if (index != 0)
					return null;
				return new Task<Object, Object>() {
					@Override
					public void run(Object value) {
						mValue = value;
					}

					@Override
					public void onFailed(Bundle value, Exception exp) {
					}
				};
			}
		};
		task.mIndex = 0;

		task.next("aaa");
		assertEquals("aaa", mValue);
	}

	public void testFail() {
		Task<String, String> task = new Task<String, String>() {
			@Override
			public void run(String value) {
			}

			@Override
			public void onFailed(Bundle value, Exception exp) {
			}
		};

		task.mPromiseContext = new MockPromiseContext() {
			@Override
			public State getState() {
				return State.ALIVE;
			}

			@Override
			public void fail(Bundle value, Exception exception) {
				mValue = value;
			}
		};

		Bundle bundle = new Bundle();
		task.fail(bundle, null);
		assertEquals(bundle, mValue);
	}

	public void testYield() {
		Task<String, String> task = new Task<String, String>() {
			@Override
			public void run(String value) {
			}
		};

		task.mPromiseContext = new MockPromiseContext() {
			@Override
			public State getState() {
				return State.ALIVE;
			}

			@Override
			public void yield(int code, Bundle value) {
				mValue = value;
			}
		};

		Bundle bundle = new Bundle();
		task.yield(1, bundle);
		assertEquals(bundle, mValue);
	}
}
