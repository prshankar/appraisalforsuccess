package com.cfs.controller.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CFSApiError {

	private HttpStatus status;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp;

	private String message;

	private String errorCode;

	private CFSApiError() {
		timestamp = LocalDateTime.now();
	}

	CFSApiError(HttpStatus status) {
		this();
		this.status = status;
	}

	CFSApiError(HttpStatus status, Throwable ex, String errorCode) {
		this();
		this.status = status;
		this.message = "Unexpected error";
		this.errorCode = errorCode;
	}

	CFSApiError(HttpStatus status, String message, Throwable ex, String errorCode) {
		this();
		this.status = status;
		this.message = message;
		this.errorCode = errorCode;
	}

	CFSApiError(HttpStatus status, String message, String errorCode) {
		this();
		this.status = status;
		this.message = message;
		this.errorCode = errorCode;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
}