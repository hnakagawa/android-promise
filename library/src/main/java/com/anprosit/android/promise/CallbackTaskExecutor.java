package com.anprosit.android.promise;

import android.os.Handler;

/**
 * Created by hnakagawa on 14/09/09.
 */
class CallbackTaskExecutor<I, O> extends HandlerThreadTaskExecutor<I, O> {
	private final Callback<O> mCallback;

	CallbackTaskExecutor(Task task, int index, Handler handler, Callback<O> callback) {
		super(task, index, 0, handler);
		mCallback = callback;
	}

	@Override
	public void run(final I value) {
		if (mCallback == null)
			return;

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mPromise.getState() != Promise.State.RUNNING)
					return;
				mCallback.onSuccess((O)value);
			}
		});
	}
}
