package com.github.bamirov.vunion.exceptions;

import com.github.bamirov.vunion.version.VGraphVersion;

public class GraphVersionMismatchException extends GraphMismatchException {
	private static final long serialVersionUID = -2643898949637359857L;
	
	private VGraphVersion<?> version;
	
	public VGraphVersion<?> getVersion() {
		return version;
	}

	public GraphVersionMismatchException(VGraphVersion<?> version) {
		super();
		this.version = version;
	}

	public GraphVersionMismatchException(VGraphVersion<?> version, String message, Throwable cause, 
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.version = version;
	}

	public GraphVersionMismatchException(VGraphVersion<?> version, String message, Throwable cause) {
		super(message, cause);
		this.version = version;
	}

	public GraphVersionMismatchException(VGraphVersion<?> version, String message) {
		super(message);
		this.version = version;
	}

	public GraphVersionMismatchException(VGraphVersion<?> version, Throwable cause) {
		super(cause);
		this.version = version;
	}
}
