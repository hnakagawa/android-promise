package com.anprosit.android.promise.example;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.widget.Toast;

import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.ResultCallback;
import com.anprosit.android.promise.Task;
import com.anprosit.android.promise.internal.utils.ThreadUtils;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mHandler = new Handler();

		Promise<String, String> promise = Promise.newInstance(this, String.class);
		promise.thenOnMainThread(new Task<String, String>() {
			@Override
			public void run(String value) {
				// on main thread
				dummyCall(new DummyCallback<String>() {
					@Override
					public void onResult(String result) {
						yield(1, null);
						next(result);
					}
				}, value);
			}
		}).thenOnAsyncThread(new Task<String, String>() {
			@Override
			public void run(String value) {
				// on async thread
				next(dummyBlockingCall(value));
				// delay 1000 milli sec
			}
		}, 1000).then(createPromise()).then(new Task<String, String>() {
			@Override
			public void run(String value) {
				// on previous task thread
				throw new RuntimeException("dummy exception");
			}

			@Override
			public void onFailed(Bundle value, Exception exception) {
				// on previous task thread
				// recovery
				next("bbb");
			}
		}).execute("aaa", new ResultCallback<String>() {
			@Override
			public void onCompleted(String result) {
				Toast.makeText(MainActivity.this, "completed with " + result, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailed(Bundle result, Exception exception) {
				Toast.makeText(MainActivity.this, "failed with " + result, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onYield(int code, Bundle value) {
				Toast.makeText(MainActivity.this, "yielded with " + code, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private Promise<String, String> createPromise() {
		Promise<String, String> promise = Promise.newInstance(this, String.class);
		promise.thenOnMainThread(new Task<String, String>() {
			@Override
			public void run(String value) {
				Toast.makeText(MainActivity.this, "modularize task with " + value, Toast.LENGTH_SHORT).show();
				next(value);
			}
		});
		return promise;
	}

	@Override
	protected void onDestroy() {
		Promise.destroy(this);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void dummyCall(final DummyCallback<String> callback, final String value) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				callback.onResult(value);
			}
		});
	}

	private String dummyBlockingCall(String value) {
		ThreadUtils.checkNotMainThread();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException exp) {
		}
		return value;
	}

	public interface DummyCallback<T> {
		public void onResult(T result);
	}
}
