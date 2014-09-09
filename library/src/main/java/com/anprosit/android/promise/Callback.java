package com.anprosit.android.promise;

import android.os.Bundle;

/**
 * Created by Hirofumi Nakagawa on 13/07/15.
 */
public interface Callback<T> {
	public void onSuccess(T result);

	public void onFailure(Bundle result, Exception exception);
}
