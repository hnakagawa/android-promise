package com.anprosit.android.promise;

import android.os.Bundle;

import com.anprosit.android.promise.internal.PromiseContext;

/**
 * Created by Hirofumi Nakagawa on 13/07/12.
 */
public abstract class Task<T, V> {
	private PromiseContext mContext;

	public abstract void run(T value);

	public void onFailed(Bundle bundle, Exception exception) {
	}

	public void onYield(int code, Bundle value) {
	}

	public void execute(T value, PromiseContext context) {
		if (context.getState() != PromiseContext.State.DOING)
			return;

		setContext(context);
		try {
			run(value);
		} catch (Exception exp) {
			fail(null, exp);
		}
	}

	protected synchronized final void next(V value) {
		try {
			PromiseContext context = getContext();
			if (context == null)
				throw new IllegalStateException(); //TODO message

			Task<V, ?> next = (Task<V, ?>) context.getNextTask();
			if (next != null)
				next.execute(value, context);
			else
				context.done(value);
		} finally {
			setContext(null);
		}
	}

	protected void fail(Bundle value) {
		fail(value, null);
	}

	protected synchronized void fail(Bundle value, Exception exception) {
		try {
			callOnFailed(value, exception);
		} finally {
			setContext(null);
		}
	}

	private void callOnFailed(Bundle value, Exception exception) {
		try {
			onFailed(value, exception);
		} finally {
			PromiseContext context = getContext();
			if (context != null)
				context.fail(value, exception);
			//else{} recovered by onFailed handler
		}
	}

	protected synchronized void yield(int code, Bundle value) {
		try {
			onYield(code, value);
			PromiseContext context = getContext();
			if (context != null)
				context.yield(code, value);
		} catch (Exception exp) {
			callOnFailed(null, exp);
		}
	}

	protected synchronized PromiseContext getContext() {
		return mContext;
	}

	protected synchronized void setContext(PromiseContext context) {
		mContext = context;
	}
}
