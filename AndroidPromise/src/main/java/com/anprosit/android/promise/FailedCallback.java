package com.anprosit.android.promise;

/**
 * Created by Hirofumi Nakagawa on 13/07/15.
 */
public interface FailedCallback<T> {
    public void run(T result, Exception exception);
}
