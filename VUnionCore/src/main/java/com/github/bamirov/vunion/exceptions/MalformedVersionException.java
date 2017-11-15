package com.github.bamirov.vunion.exceptions;

public class MalformedVersionException extends Exception {
	private static final long serialVersionUID = 7371809565384449834L;

	public MalformedVersionException() {
		super();
	}

	public MalformedVersionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public MalformedVersionException(String message, Throwable cause) {
		super(message, cause);
	}

	public MalformedVersionException(String message) {
		super(message);
	}

	public MalformedVersionException(Throwable cause) {
		super(cause);
	}
}
