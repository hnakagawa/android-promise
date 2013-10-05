package com.anprosit.android.promise;

import android.os.Handler;
import android.os.Looper;

import com.anprosit.android.promise.internal.PromiseImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Created by Hirofumi Nakagawa on 13/07/12.
 */
public abstract class Promise<I, O> {
	private static Map<Object, Set<Promise<?, ?>>> mPromises = new WeakHashMap<Object, Set<Promise<?, ?>>>();

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

	public static synchronized <T> Promise<T, T> newInstance(Object lifecycle, Class<T> in) {
		return newInstance(lifecycle, in, new Handler(Looper.getMainLooper()));
	}

	public static synchronized <T> Promise<T, T> newInstance(Object lifecycle, Class<T> in, Handler handler) {
		Set<Promise<?, ?>> set = mPromises.get(lifecycle);
		if (set == null)
			set = new HashSet<Promise<?, ?>>();

		Promise instance = new PromiseImpl(handler);
		set.add(instance);
		return instance;
	}

    public static synchronized void destroy(Object lifecycle, Promise<?, ?> promise) {
        try {
            Set<Promise<?, ?>> set = mPromises.remove(lifecycle);
            if (set == null)
                return;
            set.remove(promise);
        } finally {
            promise.destroy();
        }
    }

	public static synchronized void destroy(Object lifecycle) {
		Set<Promise<?, ?>> set = mPromises.remove(lifecycle);
		if (set == null)
			return;

		for (Promise<?, ?> promise : set)
			promise.destroy();
	}
}
