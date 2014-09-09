package com.anprosit.android.promise;

/**
 * Created by Hirofumi Nakagawa on 13/07/14.
 */
abstract class DelayTaskExecutor<I, O> extends TaskExecutor<I, O> {
	private final long mDelay;

	DelayTaskExecutor(Task<I, O> task, int index, long delay) {
		super(task, index);
		mDelay = delay;
	}

	public long getDelay() {
		return mDelay;
	}
}
