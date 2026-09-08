package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.stereotype.Service;

import com.example.demo.dto.UserCsvRecord;
import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.exception.AddressEnrichmentException;
import com.example.demo.repository.UserRepository;

import jakarta.validation.ConstraintViolation;
@Service
public class CsvRecordProcessingService {
	private final Executor executor;
	private final ValidateRecordService validateRecordService;
	private final AddressEnrichmentService addressEnrichmentService;
	private final UserRepository repository;
	public CsvRecordProcessingService(Executor executor,UserRepository repository,ValidateRecordService validateRecordService,AddressEnrichmentService addressEnrichmentService) {
		this.executor=executor;
		this.validateRecordService=validateRecordService;
		this.addressEnrichmentService=addressEnrichmentService;
		this.repository=repository;
	}
	List<CompletableFuture<User>> futures = new ArrayList<>();
	public CompletableFuture<User> processRecord(Set<ConstraintViolation<UserCsvRecord>> violations,UserCsvRecord userCsvRecord){
	 CompletableFuture<User> future = CompletableFuture.supplyAsync(()->{
			try { 
			if(violations.isEmpty()) {
			String zip = userCsvRecord.getZipCode();
			Address address = addressEnrichmentService.addressSearchByApi(zip);
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
			validateRecordService.storeViolations(violations, userCsvRecord);
			return null;
			}
			catch(AddressEnrichmentException e) {
				 String reason =
					        "Email: " + userCsvRecord.getEmail()
					        + " | Zip: " + userCsvRecord.getZipCode()
					        + " | Reason: " + e.getMessage();

					    validateRecordService.displayViolations().add(reason);
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
		    return future;
	}
	public List<User> addValidUser(List<CompletableFuture<User>> futures) {
	CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
	List<User> users = futures.stream()
			                  .map(CompletableFuture::join)
			                  .filter(user->user!=null)
			                  .toList();
    Objects.requireNonNull(users);
	repository.saveAll(users); 
	return users;
	}

}
