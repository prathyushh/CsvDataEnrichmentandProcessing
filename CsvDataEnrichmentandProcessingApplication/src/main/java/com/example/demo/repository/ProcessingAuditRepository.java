package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ProcessingAudit;

public interface ProcessingAuditRepository extends JpaRepository<ProcessingAudit, Long>{

}
