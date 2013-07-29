package com.anprosit.android.promise.internal;

import android.os.Bundle;
import android.os.Handler;

import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.ResultCallback;
import com.anprosit.android.promise.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Hirofumi Nakagawa on 13/07/12.
 */
public class PromiseImpl<I, O> extends Promise<I, O> implements PromiseContext {
	private List<Task<?, ?>> mTasks = new ArrayList<Task<?, ?>>();

	private final Handler mHandler;

	private ResultCallback<?> mResultCallback;

	protected volatile State mState = State.ALIVE;

	public PromiseImpl(Handler handler) {
		mHandler = handler;
	}

	@Override
	public synchronized <NO> Promise<I, NO> then(Task<O, NO> task) {
		addTask(task);
		return (Promise<I, NO>) this;
	}

	@Override
	public synchronized <NO> Promise<I, NO> then(Promise<O, NO> promise) {
		addTasks(promise.anatomy());
		return (Promise<I, NO>) this;
	}

	@Override
	public <NO> Promise<I, NO> thenOnMainThread(Task<O, NO> task) {
		return thenOnMainThread(task, 0);
	}

	@Override
	public synchronized <NO> Promise<I, NO> thenOnMainThread(Task<O, NO> task, long delay) {
		addTask(new HandlerThreadTask(delay));
		addTask(task);
		return (Promise<I, NO>) this;
	}

	@Override
	public <NO> Promise<I, NO> thenOnAsyncThread(Task<O, NO> task) {
		return thenOnAsyncThread(task, 0);
	}

	@Override
	public synchronized <NO> Promise<I, NO> thenOnAsyncThread(Task<O, NO> task, long delay) {
		addTask(new AsyncThreadTask(delay));
		addTask(task);
		return (Promise<I, NO>) this;
	}

	@Override
	public synchronized void execute(I value, ResultCallback<O> resultCallback) {
		mResultCallback = resultCallback;

		Task<Object, ?> next = (Task<Object, ?>) getTask(0);

		if (next == null) {
			done(value);
			return;
		}

		next.execute(value, this, 1);
	}

	@Override
	public synchronized Collection<Task<?, ?>> anatomy() {
		if (getState() != State.ALIVE)
			throw new IllegalStateException("Promise#anatomy method must be called in DOING state");
		destroy();
		return mTasks;
	}

	@Override
	public synchronized void done(final Object result) {
		if (getState() != State.ALIVE)
			return;

		final ResultCallback<Object> callback = (ResultCallback<Object>) mResultCallback;
		if (callback == null)
			return;

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (getState() != State.ALIVE)
					return;

				callback.onCompleted(result);
			}
		});
	}

	@Override
	public synchronized void cancel() {
		if (getState() != State.ALIVE)
			return;
		setState(State.CANCELLED);
	}

	@Override
	public synchronized boolean isCancelled() {
		return getState() == State.CANCELLED;
	}

	@Override
	public synchronized void destroy() {
		setState(State.DESTROYED);
	}

	@Override
	public synchronized void fail(final Bundle result, final Exception exception) {
		if (getState() != State.ALIVE)
			return;

		final ResultCallback<?> callback = mResultCallback;
		if (callback == null)
			return;

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (getState() != State.ALIVE)
					return;

				callback.onFailed(result, exception);
			}
		});
	}

	@Override
	public synchronized void yield(final int code, final Bundle value) {
		if (getState() != State.ALIVE)
			return;

		final ResultCallback<?> callback = mResultCallback;
		if (callback == null)
			return;

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (getState() == State.ALIVE)
					callback.onYield(code, value);
			}
		});
	}

	@Override
	public State getState() {
		return mState;
	}

	protected void setState(State state) {
		mState = state;
	}

	@Override
	public synchronized Task<?, ?> getTask(int index) {
		if (index >= mTasks.size())
			return null;
		return mTasks.get(index);
	}

	private void addTask(Task<?, ?> task) {
		mTasks.add(task);
	}

	private void addTasks(Collection<Task<?, ?>> tasks) {
		mTasks.addAll(tasks);
	}
}
