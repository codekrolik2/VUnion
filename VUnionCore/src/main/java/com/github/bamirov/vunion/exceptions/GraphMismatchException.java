package com.github.bamirov.vunion.exceptions;

public class GraphMismatchException extends Exception {
	private static final long serialVersionUID = -3072807962120568345L;

	public GraphMismatchException() {
		super();
	}

	public GraphMismatchException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public GraphMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public GraphMismatchException(String message) {
		super(message);
	}

	public GraphMismatchException(Throwable cause) {
		super(cause);
	}
}
