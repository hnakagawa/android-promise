package com.anprosit.android.promise;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.anprosit.android.promise.internal.PromiseImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by Hirofumi Nakagawa on 13/07/12.
 */
public abstract class Promise<I, O> {
	private static Map<Context, Set<Promise<?, ?>>> mPromises = new WeakHashMap<Context, Set<Promise<?, ?>>>();

	public abstract <NO> Promise<I, NO> then(Task<O, NO> task);

	public abstract <NO> Promise<I, NO> then(Promise<O, NO> promise);

	public abstract <NO> Promise<I, NO> thenOnMainThread(Task<O, NO> task);

	public abstract <NO> Promise<I, NO> thenOnMainThread(Task<O, NO> task, long delay);

	public abstract <NO> Promise<I, NO> thenOnAsyncThread(Task<O, NO> task);

	public abstract <NO> Promise<I, NO> thenOnAsyncThread(Task<O, NO> task, long delay);

	public abstract Collection<Task<?, ?>> anatomy();

    public abstract PromiseExecutor<I, O> setResultCallback(ResultCallback<O> resultCallback);

    public abstract void execute(I value);

	public abstract void execute(I value, ResultCallback<O> resultCallback);

	public abstract void cancel();

	public abstract void destroy();

	public abstract boolean isCancelled();

	public static synchronized <T> Promise<T, T> newInstance(Context context, Class<T> in) {
		return newInstance(context, in, new Handler(Looper.getMainLooper()));
	}

	public static synchronized <T> Promise<T, T> newInstance(Context context, Class<T> in, Handler handler) {
		Set<Promise<?, ?>> set = mPromises.get(context);
		if (set == null)
			set = new HashSet<Promise<?, ?>>();

		Promise instance = new PromiseImpl(handler);
		set.add(instance);
		return instance;
	}

    public static synchronized void destroy(Context context, Promise<?, ?> promise) {
        try {
            Set<Promise<?, ?>> set = mPromises.remove(context);
            if (set == null)
                return;
            set.remove(promise);
        } finally {
            promise.destroy();
        }
    }

	public static synchronized void destroy(Context context) {
		Set<Promise<?, ?>> set = mPromises.remove(context);
		if (set == null)
			return;

		for (Promise<?, ?> promise : set)
			promise.destroy();
	}
}
