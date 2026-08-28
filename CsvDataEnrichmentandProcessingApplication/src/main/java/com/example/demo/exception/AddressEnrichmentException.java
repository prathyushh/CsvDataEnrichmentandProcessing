package com.example.demo.exception;

public class AddressEnrichmentException extends RuntimeException{
       public AddressEnrichmentException(String message) {
    	   super(message);
       }
       public AddressEnrichmentException(String message,Throwable cause) {
    	   super(message,cause);
       }
}
