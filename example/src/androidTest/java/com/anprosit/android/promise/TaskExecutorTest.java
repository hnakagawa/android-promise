package com.anprosit.android.promise;

import android.os.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by hnakagawa on 14/09/09.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TaskExecutorTest {
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void run_shouldCallTasksRun() {
		TaskExecutor<Integer, Integer> next = mock(TaskExecutor.class);

		Promise<Integer, Integer> promise = mock(Promise.class);
		when(promise.getState()).thenReturn(Promise.State.RUNNING);
		when(promise.getTaskExecutor(2)).thenReturn((TaskExecutor)next);

		Task<Integer, Integer> task = mock(Task.class);
		TaskExecutor<Integer, Integer> executor = new TaskExecutor<Integer, Integer>(task, 1);
		executor.setPromise(promise);
		executor.run(0);
		verify(task).run(0, next);
	}

	@Test
	public void run_shouldCallPromisesYield() {
		final Bundle bundle = new Bundle();
		bundle.putString("a", "A");

		Promise<Integer, Integer> promise = mock(Promise.class);
		when(promise.getState()).thenReturn(Promise.State.RUNNING);

		Task<Integer, Integer> nextTask = new Task<Integer, Integer>() {
			@Override
			public void run(Integer value, NextTask<Integer> next) {
				next.yield(3, bundle);
			}
		};

		TaskExecutor<Integer, Integer> nextExecutor = new TaskExecutor<Integer, Integer>(nextTask, 2);
		nextExecutor.setPromise(promise);
		when(promise.getTaskExecutor(2)).thenReturn((TaskExecutor)nextExecutor);

		TaskExecutor<Integer, Integer> executor = new TaskExecutor<Integer, Integer>(nextTask, 1);
		executor.setPromise(promise);
		executor.run(0);
		verify(promise).yield(3, bundle);
	}

	@Test
	public void run_shouldCallPromisesFail() {
		final Bundle bundle = new Bundle();
		bundle.putString("a", "A");

		final RuntimeException exp = new RuntimeException();

		Promise<Integer, Integer> promise = mock(Promise.class);
		when(promise.getState()).thenReturn(Promise.State.RUNNING);

		Task<Integer, Integer> nextTask = new Task<Integer, Integer>() {
			@Override
			public void run(Integer value, NextTask<Integer> next) {
				next.fail(bundle, exp);
			}
		};

		TaskExecutor<Integer, Integer> nextExecutor = new TaskExecutor<Integer, Integer>(nextTask, 2);
		nextExecutor.setPromise(promise);
		when(promise.getTaskExecutor(2)).thenReturn((TaskExecutor)nextExecutor);

		TaskExecutor<Integer, Integer> executor = new TaskExecutor<Integer, Integer>(nextTask, 1);
		executor.setPromise(promise);
		executor.run(0);
		verify(promise).fail(bundle, exp);
	}

	@Test
	public void run_shouldCallPromisesFailWithException() {
		final RuntimeException exp = new RuntimeException();

		Promise<Integer, Integer> promise = mock(Promise.class);
		when(promise.getState()).thenReturn(Promise.State.RUNNING);

		Task<Integer, Integer> nextTask = new Task<Integer, Integer>() {
			@Override
			public void run(Integer value, NextTask<Integer> next) {
				throw exp;
			}
		};

		TaskExecutor<Integer, Integer> nextExecutor = new TaskExecutor<Integer, Integer>(nextTask, 2);
		nextExecutor.setPromise(promise);
		when(promise.getTaskExecutor(2)).thenReturn((TaskExecutor)nextExecutor);

		TaskExecutor<Integer, Integer> executor = new TaskExecutor<Integer, Integer>(nextTask, 1);
		executor.setPromise(promise);
		executor.run(0);
		verify(promise).fail(null, exp);
	}
}
