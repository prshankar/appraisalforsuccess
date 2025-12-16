package com.cfs.exception;

public enum ErrorCodes {

	ERROR_001, // Error while creating stateful Soap Connector
	ERROR_002, // Soap fault exception while connecting to SOAP web service.
	ERROR_003, // Error while connecting to REST Service.
	ERROR_004, // Error due to calling SOAP service on wrong soap connector. 
	ERROR_005, // Error while unmarshalling the object
	ERROR_006, // Error while marshalling the object
	ERROR_007, // Error while getWorkBenchData. Security Fix
	ERROR_008 // Force first time password reset
}
