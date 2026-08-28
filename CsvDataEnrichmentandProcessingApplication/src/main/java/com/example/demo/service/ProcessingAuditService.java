package com.example.demo.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.demo.entity.ProcessingAudit;
import com.example.demo.repository.ProcessingAuditRepository;



@Service
public class ProcessingAuditService {

	private final ProcessingAuditRepository repository;
	public ProcessingAuditService(ProcessingAuditRepository repository) {
		this.repository=repository;
	}
	
	public ProcessingAudit startAudit(String filename) {
		ProcessingAudit audit = new ProcessingAudit();
		audit.setFilename(filename);
		audit.setStatus("Processing");
		audit.setStartTime(LocalDateTime.now());
		return repository.save(audit);
	}
	public void completeAudit(ProcessingAudit audit,int totalRecords,int successfulRecords) {
		audit.setStatus("COMPLETED");
		audit.setEndTime(LocalDateTime.now());
		audit.setTotalRecords(totalRecords);
		audit.setSuccessfulRecords(successfulRecords);
		audit.setFailedRecords(totalRecords-successfulRecords);
		repository.save(audit);
	}
	public void failAudit(ProcessingAudit audit) {
	    audit.setStatus("FAILED");
	    audit.setEndTime(LocalDateTime.now());
	    repository.save(audit);
	}
}
