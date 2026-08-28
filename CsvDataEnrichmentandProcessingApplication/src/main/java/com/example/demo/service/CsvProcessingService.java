package com.example.demo.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.UserCsvRecord;
import com.example.demo.entity.Address;
import com.example.demo.entity.ProcessingAudit;
import com.example.demo.entity.User;
import com.example.demo.exception.AddressEnrichmentException;
import com.example.demo.exception.CsvProcessingException;
import com.example.demo.exception.InvalidCsvFileException;

import com.example.demo.repository.UserRepository;

import jakarta.validation.ConstraintViolation;

import jakarta.validation.Validator;


@Service
public class CsvProcessingService {
	private final Validator validator;
	private final Executor executor;
	private final UserRepository repository;
	private final AddressEnrichmentService service;
	private final ProcessingAuditService auditService;
	public CsvProcessingService(Validator validator,UserRepository repository,AddressEnrichmentService service,Executor executor,ProcessingAuditService auditService) {
		this.validator=validator;
		this.repository=repository;
		this.service=service;
		this.executor=executor;
		this.auditService=auditService;
		
	}
	private Set<ConstraintViolation<UserCsvRecord>> validateRecord(UserCsvRecord record){
		return validator.validate(record);
	}
	
	
	public String csvParse(MultipartFile file) {
	
		if(file.isEmpty()) {
			throw new InvalidCsvFileException("Csv file is empty");
		}
		if(file.getOriginalFilename()==null || !file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
			throw new InvalidCsvFileException("Invalid file format, Please upload a csv file");
		}
		ProcessingAudit audit = auditService.startAudit(file.getOriginalFilename());
		List<CompletableFuture<User>> futures = new ArrayList<>();
		List<String> failedRecords = new CopyOnWriteArrayList<>();
		try {
		try(Reader reader = new InputStreamReader(file.getInputStream(),StandardCharsets.UTF_8)){
			CSVFormat format = CSVFormat.DEFAULT.builder()
					                            .setHeader()
					                            .setSkipHeaderRecord(true)
					                            .get();
			
			try(CSVParser parser = format.parse(reader)){
				for(CSVRecord record : parser) {
					UserCsvRecord userCsvRecord = new UserCsvRecord(
							record.get("firstName"),
							record.get("lastName"),
							record.get("zipcode"),
							record.get("phone1"),
							record.get("phone2"),
							record.get("email"),
							record.get("web")		
							);
				Set<ConstraintViolation<UserCsvRecord>> violations = validateRecord(userCsvRecord);
			    CompletableFuture<User> future = CompletableFuture.supplyAsync(()->{
				try { 
				if(violations.isEmpty()) {
				String zip = userCsvRecord.getZipCode();
				Address address = service.addressSearchByApi(zip);
				User user = new User();
				user.setFirstName(userCsvRecord.getFirstName());
				user.setLastName(userCsvRecord.getLastName());
				user.setPhone1(userCsvRecord.getPhone1());
				user.setPhone2(userCsvRecord.getPhone2());
				user.setEmail(userCsvRecord.getEmail());
				user.setWeb(userCsvRecord.getWeb());
				user.setAddress(address);
				return user;
				}
				for(ConstraintViolation<UserCsvRecord> violation : violations) {
					  String reason =
						        "Email: " + userCsvRecord.getEmail()
						        + " | Field: " + violation.getPropertyPath()
						        + " | Reason: " + violation.getMessage();

						    failedRecords.add(reason);
				}
				return null;
				}
				catch(AddressEnrichmentException e) {
					 String reason =
						        "Email: " + userCsvRecord.getEmail()
						        + " | Zip: " + userCsvRecord.getZipCode()
						        + " | Reason: " + e.getMessage();

						    failedRecords.add(reason);
					return null;
				}
				catch (Exception e) {
				    System.out.println(
				        "Unexpected error for email: "
				        + userCsvRecord.getEmail()
				        + " | Reason: "
				        + e.getMessage()
				    );
				    throw new RuntimeException(e);
				}
				},executor);
			    futures.add(future);
				}
			}
		}
		catch(IOException ex) {
			throw new CsvProcessingException("failed to process CSV file",ex);
		}
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		List<User> users = futures.stream()
				                  .map(CompletableFuture::join)
				                  .filter(user->user!=null)
				                  .toList();

		repository.saveAll(users); 
		System.out.println("Failed Records");
		failedRecords.forEach(System.out::println);
	    auditService.completeAudit(audit, futures.size(), users.size());
	    
	    return "Total Records:"+audit.getTotalRecords()+"\nSuccessful Records:"+audit.getSuccessfulRecords()+"\nFailed Records:"+audit.getFailedRecords()+"\nProcessing Time:"+Duration.between(audit.getStartTime(), audit.getEndTime()).toMillis()+"ms"; 
		}
		catch(Exception e) {
			auditService.failAudit(audit);
			throw new CsvProcessingException("Failed to process CSV file",e);
		}
		}
	
	

}
