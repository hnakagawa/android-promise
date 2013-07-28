package com.anprosit.android.promise.internal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Created by Hirofumi Nakagawa on 13/07/14.
 */
public class HandlerThreadTask<T, V> extends DelayTask<T, V> {
	private static final String TAG = HandlerThreadTask.class.getSimpleName();

	private final Handler mHandler;

	public HandlerThreadTask(Handler handler, long delay) {
		super(delay);
		mHandler = handler;
	}

	public HandlerThreadTask(long delay) {
		this(new Handler(Looper.getMainLooper()), delay);
	}

	@Override
	public void execute(final T value, final PromiseContext context) {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					if (context.getState() != PromiseContext.State.DOING)
						return;
					setContext(context);
					HandlerThreadTask.this.run(value);
				} catch (Exception exp) {
					fail(null, exp);
				}
			}
		}, getDelay());
	}

	@Override
	public void run(T value) {
		next((V) value);
	}

	@Override
	public void onFailed(Bundle value, Exception exp) {
		Log.w(TAG, exp.getMessage() + "", exp);
	}
}
