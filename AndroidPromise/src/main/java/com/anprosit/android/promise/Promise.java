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
public abstract class Promise {
    private static Map<Context, List<Promise>> mPromises = new WeakHashMap<Context, List<Promise>>();

    public abstract Promise then(Task<?, ?, ?> task);

    public abstract Promise then(Promise promise);

    public abstract Promise thenOnMainThread(Task<?, ?, ?> task);

    public abstract Promise thenOnMainThread(Task<?, ?, ?> task, long delay);

    public abstract Promise thenOnAsyncThread(Task<?, ?, ?> task);

    public abstract Promise thenOnAsyncThread(Task<?, ?, ?> task, long delay);

    public abstract Collection<Task<?, ?, ?>> anatomy();

    public abstract void execute(Object value, ResultCallback<?, ?> resultCallback);

    public abstract boolean isCompleted();

    public abstract boolean isFailed();

    public abstract void cancel();

    public abstract void destroy();

    public abstract boolean isCancelled();

    public abstract void await();

    public static synchronized Promise newInstance(Context context) {
        return newInstance(context, new Handler(Looper.getMainLooper()));
    }

    public static synchronized Promise newInstance(Context context, Handler handler) {
        List<Promise> list = mPromises.get(context);
        if (list == null)
            list = new ArrayList<Promise>();

        Promise instance = new PromiseImpl(handler);
        list.add(instance);
        return instance;
    }

    public static synchronized void destroy(Context context) {
        List<Promise> list = mPromises.remove(context);
        if (list == null)
            return;

        for (Promise promise : list)
            promise.destroy();
    }
}
