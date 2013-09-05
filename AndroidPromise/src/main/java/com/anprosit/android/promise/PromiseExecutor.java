package com.anprosit.android.promise;

/**
 * Created by hnakagawa on 13/09/05.
 */
public interface PromiseExecutor<I, O> {
    public void execute(I value);
}
