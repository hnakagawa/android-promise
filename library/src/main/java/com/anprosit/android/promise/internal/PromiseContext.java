package com.anprosit.android.promise.internal;

import android.os.Bundle;

import com.anprosit.android.promise.Task;

/**
 * Created by Hirofumi Nakagawa on 13/07/14.
 */
public interface PromiseContext {
	public void cancel();

	public void done(Object result);

	public void fail(Bundle result, Exception exception);

	public void yield(int code, Bundle value);

	public Task<?, ?> getTask(int index);

	public State getState();

	public enum State {
		ALIVE,
		CANCELLED,
		DESTROYED,
	}
}
