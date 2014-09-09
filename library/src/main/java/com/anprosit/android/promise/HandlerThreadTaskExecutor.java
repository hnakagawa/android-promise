package com.anprosit.android.promise;

import android.os.Handler;

/**
 * Created by Hirofumi Nakagawa on 13/07/14.
 */
class HandlerThreadTaskExecutor<I, O> extends DelayTaskExecutor<I, O> {
	private static final String TAG = HandlerThreadTaskExecutor.class.getSimpleName();

	protected final Handler mHandler;

	HandlerThreadTaskExecutor(Task<I, O> task, int index, long delay, Handler handler) {
		super(task, index, delay);
		mHandler = handler;
	}

	@Override
	public void run(final I value) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mPromise.getState() != Promise.State.RUNNING)
					return;

				NextTask<O> next = getNextTask();
				try {
					mTask.run(value, next);
				} catch (Exception exp) {
					next.fail(null, exp);
				}
			}
		}, getDelay());
	}
}
