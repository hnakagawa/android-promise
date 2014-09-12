package com.anprosit.android.promise;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by hnakagawa on 14/09/11.
 */
class ParallelTaskExecutor<I, O> extends TaskExecutor<I, O> {
	private final ExecutorService mService;

	ParallelTaskExecutor(TaskSet<I, O> tasks, int index, ExecutorService service) {
		super(tasks, index);
		mService = service;
	}

	@Override
	public void run(final I value) {
		if (mPromise.getState() != Promise.State.RUNNING)
			return;

		mService.execute(new Runnable() {
			@Override
			public void run() {
				TaskSet<I, O> set = (TaskSet<I, O>) mTask;
				int size = set.size();

				List<Future<ResultHolder<O>>> futures = new ArrayList<Future<ResultHolder<O>>>(size);
				//List<ResultHolder<O>> resultHolders = new ArrayList<ResultHolder<O>>(set.size());

				List<O> results = new ArrayList<O>(size);
				ArrayList<Exception> exceptions = new ArrayList<Exception>(size);
				ArrayList<Bundle> errorResults = new ArrayList<Bundle>(size);

				for (int i = 0; i < size; i++)
					futures.add(mService.submit(new ParallelCallable<I>(set.getTask(i), value, new ParallelNextTask<O>())));

				boolean hasError = false;
				for (int i = 0; i < size; i++) {
					try {
						ResultHolder<O> resultHolder = futures.get(i).get();
						results.add(resultHolder.getSuccessResult());
						errorResults.add(resultHolder.getErrorResults());
						exceptions.add(resultHolder.getException());
						if (!resultHolder.isSuccess())
							hasError = true;
					} catch (Exception exp) {
						results.add(null);
						errorResults.add(null);
						exceptions.add(exp);
						hasError = true;
					}
				}

				if (hasError) {
					Bundle bundle = new Bundle();
					bundle.putSerializable(Callback.EXCEPTIONS, exceptions);
					bundle.putSerializable(Callback.RESULTS, errorResults);
					((NextTask<List<O>>) getNextTask()).fail(bundle, null);
				} else
					((NextTask<List<O>>)getNextTask()).run(results);
			}
		});
	}

	private class ParallelCallable<I> implements Callable<ResultHolder<O>> {
		private final Task<I, O> mTask;

		private final I mValue;

		private final ParallelNextTask<O> mNextTask;

		private ParallelCallable(Task<I, O> task, I value, ParallelNextTask<O> nextTask) {
			mTask = task;
			mValue = value;
			mNextTask = nextTask;
		}

		@Override
		public ResultHolder<O> call() {
			try {
				mTask.run(mValue, mNextTask);
				return mNextTask.isSuccess() ? new ResultHolder<O>(mNextTask.getSuccessResult()) : new ResultHolder<O>(mNextTask.getErrorResults(), mNextTask.getException());
			} catch (Exception exp) {
				return new ResultHolder<O>(null, exp);
			}
		}
	};

	private class ParallelNextTask<O> implements NextTask<O> {
		private O mSuccessResult;

		private Bundle mErrorResults;

		private Exception mException;

		private boolean mIsSuccess;

		@Override
		public void run(O successResult) {
			mSuccessResult = successResult;
			mIsSuccess = true;
		}

		@Override
		public void yield(int code, Bundle value) {
			mPromise.yield(code, value);
		}

		@Override
		public void fail(Bundle errorResults, Exception exp) {
			mErrorResults = errorResults;
			mException = exp;
			mIsSuccess = false;
		}

		public boolean isSuccess() {
			return mIsSuccess;
		}

		public O getSuccessResult() {
			return mSuccessResult;
		}

		public Bundle getErrorResults() {
			return mErrorResults;
		}

		public Exception getException() {
			return mException;
		}
	}

	private static class ResultHolder<O> {
		private final O mSuccessResult;

		private final Bundle mErrorResults;

		private final Exception mException;

		private final boolean mIsSuccess;

		private ResultHolder(O successResult) {
			mSuccessResult = successResult;
			mErrorResults = null;
			mException = null;
			mIsSuccess = true;
		}

		private ResultHolder(Bundle errorResults, Exception exception) {
			mSuccessResult = null;
			mErrorResults = errorResults;
			mException = exception;
			mIsSuccess = false;
		}

		public O getSuccessResult() {
			return mSuccessResult;
		}

		public Bundle getErrorResults() {
			return mErrorResults;
		}

		public Exception getException() {
			return mException;
		}

		public boolean isSuccess() {
			return mIsSuccess;
		}
	}
}
