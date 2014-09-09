package com.anprosit.android.promise.example;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.anprosit.android.promise.Callback;
import com.anprosit.android.promise.NextTask;
import com.anprosit.android.promise.OnYieldListener;
import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.Task;
import com.anprosit.android.promise.utils.ThreadUtils;

/**
 * Created by hnakagawa on 14/09/08.
 */
public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	@Override
	protected void onResume() {
		super.onResume();
		Promise.with(this, Integer.class).then(new Task<Integer, String>() {
			@Override
			public void run(Integer value, NextTask<String> next) {
				ThreadUtils.checkMainThread();
				Toast.makeText(MainActivity.this, "Input:" + value, Toast.LENGTH_SHORT).show();
				next.yield(0, null);
				next.run(value + 1 + "");
			}
		}).thenOnAsyncThread(new Task<String, Integer>() {
			@Override
			public void run(String value, NextTask<Integer> next) {
				try {
					ThreadUtils.checkNotMainThread();
					Log.i(TAG, "This task is running on the no main thread");
					Thread.sleep(1000);
					next.run(Integer.parseInt(value) + 1);
				} catch (InterruptedException e) {
					next.fail(null, e);
				}
			}
		}).then(new Task<Integer, String>() {
			@Override
			public void run(Integer value, NextTask<String> next) {
				ThreadUtils.checkNotMainThread();
				next.run(value + 1 + "");
			}
		}).thenOnMainThread(new Task<String, Integer>() {
			@Override
			public void run(String value, NextTask<Integer> next) {
				ThreadUtils.checkMainThread();
				Toast.makeText(MainActivity.this, "This task is running on main thread", Toast.LENGTH_SHORT).show();
				next.run(Integer.parseInt(value) + 1);
			}
		}).setOnYieldListener(new OnYieldListener() {
			@Override
			public void onYield(int code, Bundle bundle) {
				ThreadUtils.checkMainThread();
				Log.d(TAG, "onYield code:" + code);
			}
		}).setCallback(new Callback<Integer>() {
			@Override
			public void onSuccess(Integer result) {
				ThreadUtils.checkMainThread();
				Toast.makeText(MainActivity.this, "Completed all tasks:" + result, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailure(Bundle result, Exception exp) {
				ThreadUtils.checkMainThread();
				Toast.makeText(MainActivity.this, "Failed some task", Toast.LENGTH_SHORT).show();
				Log.e(TAG, exp.getMessage() + "", exp);
			}
		}).create().execute(1);
	}

	@Override
	protected void onPause() {
		Promise.destroyWith(this);
		super.onPause();
	}
}
