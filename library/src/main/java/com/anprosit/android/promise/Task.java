package com.anprosit.android.promise;

/**
 * Created by Hirofumi Nakagawa on 13/07/12.
 */
public interface Task<I, O> {
	public void run(I value, NextTask<O> next);
}
