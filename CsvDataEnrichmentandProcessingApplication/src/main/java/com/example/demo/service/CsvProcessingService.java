package com.example.demo.service;


import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.UserCsvRecord;
import com.example.demo.entity.ProcessingAudit;
import com.example.demo.entity.User;
import com.example.demo.exception.CsvProcessingException;
import com.example.demo.exception.InvalidCsvFileException;

import jakarta.validation.ConstraintViolation;



@Service
public class CsvProcessingService {


	
	
	private final CsvParseService csvParseService;
	private final ProcessingAuditService auditService;
	private final ValidateRecordService validateRecordService;
	private final CsvRecordProcessingService csvRecordProcessingService;
	public CsvProcessingService(CsvRecordProcessingService csvRecordProcessingService,ValidateRecordService validateRecordService,ProcessingAuditService auditService,CsvParseService csvParseService) {
		this.auditService=auditService;
		this.csvParseService=csvParseService;
		this.validateRecordService=validateRecordService;
		this.csvRecordProcessingService=csvRecordProcessingService;
	}
	
	
	
	public String csvParse(MultipartFile file) {
	    String filename = file.getOriginalFilename();
		if(file.isEmpty()) {
			throw new InvalidCsvFileException("Csv file is empty");
		}
		if(filename==null || !filename.toLowerCase().endsWith(".csv")) {
			throw new InvalidCsvFileException("Invalid file format, Please upload a csv file");
		}
		ProcessingAudit audit = auditService.startAudit(filename);
		List<CompletableFuture<User>> futures = new ArrayList<>();
		
		try {
		      
			  CSVParser parser = csvParseService.createParser(file);
			
				for(CSVRecord record : parser) {
				UserCsvRecord userCsvRecord=csvParseService.setDto(record);
				
				Set<ConstraintViolation<UserCsvRecord>> violations = validateRecordService.validateRecord(userCsvRecord);
				
			    futures.add(csvRecordProcessingService.processRecord(violations, userCsvRecord));
				}
			
		
		
		List<User> users = csvRecordProcessingService.addValidUser(futures);
		System.out.println("Failed Records");
		validateRecordService.displayViolations().forEach(System.out::println);
	    auditService.completeAudit(audit, futures.size(), users.size());
	    
	    return "Total Records:"+audit.getTotalRecords()+"\nSuccessful Records:"+audit.getSuccessfulRecords()+"\nFailed Records:"+audit.getFailedRecords()+"\nProcessing Time:"+Duration.between(audit.getStartTime(), audit.getEndTime()).toMillis()+"ms"; 
		}
		catch(Exception e) {
			auditService.failAudit(audit);
			throw new CsvProcessingException("Failed to process CSV file",e);
		}
		}
	
	

}
