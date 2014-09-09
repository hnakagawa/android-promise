package com.anprosit.android.promise;

import android.os.Bundle;

/**
 * Created by hnakagawa on 14/09/09.
 */
public interface NextTask<I> {
	public void run(I value);

	public void yield(int code, Bundle value);

	public void fail(Bundle result, Exception exp);
}
