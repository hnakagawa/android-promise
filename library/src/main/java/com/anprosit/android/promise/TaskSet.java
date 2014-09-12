package com.anprosit.android.promise;

import java.util.List;

/**
 * Created by hnakagawa on 14/09/11.
 */
final class TaskSet<I, O> implements Task<I, O> {
	private final List<Task<I, O>> mTasks;

	TaskSet(List<Task<I, O>> tasks) {
		mTasks = tasks;
	}

	@Override
	public void run(I value, NextTask<O> next) {
		throw new UnsupportedOperationException();
	}

	public Task<I, O> getTask(int index) {
		return mTasks.get(index);
	}

	public int size() {
		return mTasks.size();
	}
}
