package com.anprosit.android.promise;

import android.os.Bundle;

import com.anprosit.android.promise.internal.PromiseContext;

/**
 * Created by Hirofumi Nakagawa on 13/07/12.
 */
public abstract class Task<T, V> {
	protected volatile PromiseContext mPromiseContext;

	protected volatile int mIndex;

	public abstract void run(T value);

	public void onFailed(Bundle bundle, Exception exception) {
	}

	public void onYield(int code, Bundle value) {
	}

	public void execute(T value, PromiseContext promiseContext, int index) {
		mPromiseContext = promiseContext;
		mIndex = index;

		if (PromiseContext.State.ALIVE != mPromiseContext.getState())
			return;

		try {
			run(value);
		} catch (Exception exp) {
			fail(null, exp);
		}
	}

	protected final void next(V value) {
		if (PromiseContext.State.ALIVE != mPromiseContext.getState())
			return;

		Task<V, ?> next = (Task<V, ?>) mPromiseContext.getTask(mIndex);
		if (next != null)
			next.execute(value, mPromiseContext, mIndex + 1);
		else
			mPromiseContext.done(value);
	}

	protected void fail(Bundle value) {
		fail(value, null);
	}

	protected void fail(Bundle value, Exception exception) {
		try {
			onFailed(value, exception);
		} finally {
			mPromiseContext.fail(value, exception);
		}
	}

	protected void yield(int code, Bundle value) {
		try {
			onYield(code, value);
			mPromiseContext.yield(code, value);
		} catch (Exception exp) {
			fail(null, exp);
		}
	}
}
