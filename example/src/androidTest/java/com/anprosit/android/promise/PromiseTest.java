package com.anprosit.android.promise;

import android.os.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by hnakagawa on 14/09/09.
 */
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class PromiseTest {
	private boolean mCalled;

	@Before
	public void setUp() throws Exception {
		mCalled = false;
	}

	@After
	public void tearDown() {
	}

	@Test
	public void with_ShouldReturnCreator() {
		Promise.Creator<String, String> creator = Promise.with(this, String.class);
		assertNotNull(creator);
	}

	@Test
	public void create_shouldReturnPromise() {
		Promise<String, String> promise = Promise.with(this, String.class).create();
		assertNotNull(promise);
		assertEquals(0, promise.getAllTasks().size());
	}

	@Test
	public void getState_shouldReturnState() {
		Promise<Integer, Integer> promise = Promise.with(this, Integer.class).create();
		assertEquals(Promise.State.INIT, promise.getState());
		promise.execute(0);
		assertEquals(Promise.State.RUNNING, promise.getState());
		promise.destroy();
		assertEquals(Promise.State.DESTROYED, promise.getState());
	}

	@Test
	public void execute_shouldExecuteCallback() {
		Promise.with(this, Integer.class).setCallback(new Callback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				mCalled = true;
			}

			@Override
			public void onFailure(Bundle result, Exception exception) {
			}
		}).create().execute(0);
		Robolectric.runUiThreadTasks();
		assertTrue(mCalled);
	}

	@Test
	public void execute_shouldExecuteErrorCallback() {
		Promise.with(this, Integer.class).then(new Task<Integer, Integer>() {
			@Override
			public void run(Integer value, NextTask<Integer> next) {
				throw new RuntimeException();
			}
		}).setCallback(new Callback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
			}

			@Override
			public void onFailure(Bundle result, Exception exception) {
				mCalled = true;
			}
		}).create().execute(0);
		Robolectric.runUiThreadTasks();
		assertTrue(mCalled);
	}

	@Test
	public void execute_shouldExecuteAllTasks() {
		Promise.Creator<Integer, Integer> creator = Promise.with(this, Integer.class);
		for (int i = 0; i < 10; i++) {
			creator.then(new Task<Integer, Object>() {
				@Override
				public void run(Integer value, NextTask<Object> next) {
					next.run(value + 1);
				}
			});
		}

		creator.setCallback(new Callback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				assertEquals(10, (int)result);
				mCalled = true;
			}

			@Override
			public void onFailure(Bundle result, Exception exception) {
			}
		}).create().execute(0);
		assertTrue(mCalled);
	}
}
