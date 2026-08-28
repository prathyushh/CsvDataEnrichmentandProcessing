package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(InvalidCsvFileException.class)
	public ResponseEntity<String> handleInvalidCsvFileException(InvalidCsvFileException ex){
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(ex.getMessage());
	}
	@ExceptionHandler(CsvProcessingException.class)
	public ResponseEntity<String> handleCsvProcessingException(CsvProcessingException ex){
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ex.getMessage());
	}
	@ExceptionHandler(AddressEnrichmentException.class)
	public ResponseEntity<String> handleAddressEnrichmentException(AddressEnrichmentException ex){
		return ResponseEntity
				.status(HttpStatus.BAD_GATEWAY)
				.body(ex.getMessage());
	}
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception ex){
		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("An unexpected error occurred");
	}

}
