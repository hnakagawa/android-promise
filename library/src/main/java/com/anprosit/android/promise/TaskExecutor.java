package com.anprosit.android.promise;

import android.os.Bundle;

/**
 * Created by hnakagawa on 14/09/09.
 */
class TaskExecutor<I, O> implements NextTask<I> {
	protected final Task<I, O> mTask;

	protected final int mIndex;

	protected Promise<?, ?> mPromise;

	TaskExecutor(Task<I, O> task, int index) {
		mTask = task;
		mIndex = index;
	}

	@Override
	public void run(I value) {
		if (mPromise.getState() != Promise.State.RUNNING)
			return;

		NextTask<O> next = getNextTask();
		try {
			mTask.run(value, next);
		} catch (Exception exp) {
			next.fail(null, exp);
		}
	}

	@Override
	public final void yield(int code, Bundle value) {
		mPromise.yield(code, value);
	}

	@Override
	public final void fail(Bundle result, Exception exp) {
		mPromise.fail(result, exp);
	}

	public final void setPromise(Promise<?, ?> promise) {
		mPromise = promise;
	}

	protected final NextTask<O> getNextTask() {
		return (NextTask<O>)mPromise.getTaskExecutor(mIndex + 1);
	}
}
