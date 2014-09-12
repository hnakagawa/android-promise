package com.anprosit.android.promise;

/**
 * Created by hnakagawa on 14/09/09.
 */
final class CallbackTaskExecutor<I> extends TaskExecutor<I, Void> {
	CallbackTaskExecutor(Promise<?, I> promise) {
		super(null, -1);
		mPromise = promise;
	}

	@Override
	public void run(I value) {
		((Promise<?, I>)mPromise).done(value);
	}
}
