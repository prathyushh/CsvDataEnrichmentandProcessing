package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "audits")
public class ProcessingAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @Column(name = "file_name")
  private String filename;
  @Column(name = "status")
  private String status;
  @Column(name = "start_ime")
  private LocalDateTime startTime;
  @Column(name = "end_time")
  private LocalDateTime endTime;
  @Column(name = "total_records")
  private int totalRecords;
  @Column(name = "successful_records")
  private int successfulRecords;
  @Column(name = "failed_records")
  private int failedRecords;
}