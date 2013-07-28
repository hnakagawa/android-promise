package com.anprosit.android.promise;

import android.os.Bundle;

/**
 * Created by Hirofumi Nakagawa on 13/07/15.
 */
public interface ResultCallback<T> {
	public void onCompleted(T result);

	public void onFailed(Bundle result, Exception exception);

	public void onYield(int code, Bundle value);
}
