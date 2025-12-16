package com.cfs.exception;

import lombok.Getter;

@Getter
public class CFSException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final ErrorCodes errorCode;

	public CFSException(ErrorCodes errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public CFSException(ErrorCodes errorCode, String message, Throwable t) {
		super(message, t);
		this.errorCode = errorCode;
	}

	public CFSException(ErrorCodes errorCode, Throwable t) {
		super(t);
		this.errorCode = errorCode;
	}

	public CFSException(ErrorCodes errorCode) {
		this.errorCode = errorCode;
	}

	public ErrorCodes getErrorCode() {
		return errorCode;
	}
}
