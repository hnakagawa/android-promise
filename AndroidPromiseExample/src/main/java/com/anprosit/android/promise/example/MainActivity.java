package com.anprosit.android.promise.example;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.anprosit.android.promise.Promise;
import com.anprosit.android.promise.ResultCallback;
import com.anprosit.android.promise.Task;
import com.anprosit.android.promise.internal.utils.ThreadUtils;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private Handler mHandler;

	private Button mButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mButton = (Button) findViewById(R.id.button);

		mHandler = new Handler();

		Promise<String, String> promise = Promise.newInstance(this, String.class);
		promise.then(new Task<String, String>() {
			@Override
			public void run(final String value) {
				mButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						next(value);
					}
				});
			}
		}).thenOnAsyncThread(new Task<String, String>() {
			@Override
			public void run(String value) {
				yield(1, null);
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
		}).setResultCallback(new ResultCallback<String>() {
			@Override
			public void onCompleted(String result) {
				Log.w(TAG, "onCompleted");
				Toast.makeText(MainActivity.this, "completed with " + result, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onFailed(Bundle result, Exception exception) {
				Toast.makeText(MainActivity.this, "failed with " + exception.getMessage(), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onYield(int code, Bundle value) {
				Toast.makeText(MainActivity.this, "yielded with " + code, Toast.LENGTH_SHORT).show();
			}
        }).execute("aaa");
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

	private String dummyBlockingCall(String value) {
		ThreadUtils.checkNotMainThread();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException exp) {
		}
		return value;
	}
}
