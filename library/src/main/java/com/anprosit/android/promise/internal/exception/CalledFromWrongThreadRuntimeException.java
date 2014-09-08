package com.anprosit.android.promise.internal.exception;

/**
 * Created by Hirofumi Nakagawa on 13/07/16.
 */
public class CalledFromWrongThreadRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -7765915177820483124L;

	public CalledFromWrongThreadRuntimeException() {
		super();
	}

	public CalledFromWrongThreadRuntimeException(String detailMessage,
	                                             Throwable throwable) {
		super(detailMessage, throwable);
	}

	public CalledFromWrongThreadRuntimeException(String detailMessage) {
		super(detailMessage);
	}

	public CalledFromWrongThreadRuntimeException(Throwable throwable) {
		super(throwable);
	}
}
