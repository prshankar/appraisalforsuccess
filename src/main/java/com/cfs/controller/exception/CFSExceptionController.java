package com.cfs.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.cfs.exception.CFSException;

@ControllerAdvice
public class CFSExceptionController {

	@ExceptionHandler(value = CFSException.class)
	public ResponseEntity<Object> exception(CFSException exception) {
		return buildResponseEntity(new CFSApiError(HttpStatus.NOT_FOUND, exception.getMessage(), exception.getErrorCode().toString()));
	}

	private ResponseEntity<Object> buildResponseEntity(CFSApiError apiError) {
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}
}
