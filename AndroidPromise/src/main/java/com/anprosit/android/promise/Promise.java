package com.anprosit.android.promise;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.anprosit.android.promise.internal.PromiseImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Hirofumi Nakagawa on 13/07/12.
 */
public abstract class Promise<I, O> {
    private static Map<Context, List<Promise<?, ?>>> mPromises = new WeakHashMap<Context, List<Promise<?, ?>>>();

    public abstract <NO> Promise<I, NO> then(Task<O, NO> task);

    public abstract <NO> Promise<I, NO> then(Promise<O, NO> promise);

    public abstract <NO> Promise<I, NO> thenOnMainThread(Task<O, NO> task);

    public abstract <NO> Promise<I, NO> thenOnMainThread(Task<O, NO> task, long delay);

    public abstract <NO> Promise<I, NO> thenOnAsyncThread(Task<O, NO> task);

    public abstract <NO> Promise<I, NO> thenOnAsyncThread(Task<O, NO> task, long delay);

    public abstract Collection<Task<?, ?>> anatomy();

    public abstract void execute(I value, ResultCallback<O> resultCallback);

    public abstract boolean isCompleted();

    public abstract boolean isFailed();

    public abstract void cancel();

    public abstract void destroy();

    public abstract boolean isCancelled();

    public abstract void await();

    public static synchronized <T> Promise<T, T> newInstance(Context context, Class<T> in) {
        return newInstance(context, in, new Handler(Looper.getMainLooper()));
    }

    public static synchronized <T> Promise<T, T> newInstance(Context context, Class<T> in, Handler handler) {
        List<Promise<?, ?>> list = mPromises.get(context);
        if (list == null)
            list = new ArrayList<Promise<?, ?>>();

        Promise instance = new PromiseImpl(handler);
        list.add(instance);
        return instance;
    }

    public static synchronized void destroy(Context context) {
        List<Promise<?, ?>> list = mPromises.remove(context);
        if (list == null)
            return;

        for (Promise<?, ?> promise : list)
            promise.destroy();
    }
}
