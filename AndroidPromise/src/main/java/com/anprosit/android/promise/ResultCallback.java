package com.anprosit.android.promise;

/**
 * Created by Hirofumi Nakagawa on 13/07/15.
 */
public interface ResultCallback<T, E> {
    public void onCompleted(T result);

    public void onFailed(E result, Exception exception);
}
