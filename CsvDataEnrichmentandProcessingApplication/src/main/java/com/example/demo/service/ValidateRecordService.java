package com.example.demo.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import com.example.demo.dto.UserCsvRecord;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

@Service
public class ValidateRecordService {
	private final Validator validator;
	List<String> failedRecords = new CopyOnWriteArrayList<>();
	public ValidateRecordService(Validator validator) {
		this.validator=validator;
	}
	Set<ConstraintViolation<UserCsvRecord>> validateRecord(UserCsvRecord record){
		return validator.validate(record);
	}
	public void storeViolations(Set<ConstraintViolation<UserCsvRecord>> violations,UserCsvRecord userCsvRecord) {
	
	for(ConstraintViolation<UserCsvRecord> violation : violations) {
		  String reason =
			        "Email: " + userCsvRecord.getEmail()
			        + " | Field: " + violation.getPropertyPath()
			        + " | Reason: " + violation.getMessage();

			    failedRecords.add(reason);
	}
	}
	
	public List<String> displayViolations(){
		return failedRecords;
	}
	

}
