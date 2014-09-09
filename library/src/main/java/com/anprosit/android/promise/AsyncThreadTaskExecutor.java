package com.anprosit.android.promise;

import java.util.concurrent.Executor;

/**
 * Created by Hirofumi Nakagawa on 13/07/14.
 */
class AsyncThreadTaskExecutor<I, O> extends DelayTaskExecutor<I, O> {
	private static final String TAG = AsyncThreadTaskExecutor.class.getSimpleName();

	private final Executor mExecutor;

	AsyncThreadTaskExecutor(Task<I, O> task, int index, long delay, Executor executor) {
		super(task, index, delay);
		mExecutor = executor;
	}

	@Override
	public void run(final I value) {
		mExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (mPromise.getState() != Promise.State.RUNNING)
					return;

				NextTask<O> next = getNextTask();
				try {
					Thread.sleep(getDelay());
					mTask.run(value, next);
				} catch (Exception exp) {
					next.fail(null, exp);
				}
			}
		});
	}
}
