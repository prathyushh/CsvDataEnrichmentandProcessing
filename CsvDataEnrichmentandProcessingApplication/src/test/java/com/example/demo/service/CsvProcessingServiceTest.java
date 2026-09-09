package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.UserCsvRecord;
import com.example.demo.entity.ProcessingAudit;
import com.example.demo.entity.User;
import com.example.demo.exception.InvalidCsvFileException;

@ExtendWith(MockitoExtension.class)
class CsvProcessingServiceTest {

    @Mock
    private ProcessingAuditService auditService;

    @Mock
    private ValidateRecordService validateRecordService;

    @Mock
    private CsvRecordProcessingService csvRecordProcessingService;

    private CsvParseService csvParseService;

    private CsvProcessingService service;

    @BeforeEach
    void setUp() {

        csvParseService = new CsvParseService();

        service = new CsvProcessingService(
                csvRecordProcessingService,
                validateRecordService,
                auditService,
                csvParseService
        );
    }

    @Test
    void csvParse_shouldThrowExceptionWhenFileIsEmpty() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                new byte[0]
        );

        assertThrows(
                InvalidCsvFileException.class,
                () -> service.csvParse(file)
        );

        verifyNoInteractions(
                auditService,
                validateRecordService,
                csvRecordProcessingService
        );
    }

    @Test
    void csvParse_shouldThrowExceptionWhenFileIsNotCsv() {

        MultipartFile file = new MockMultipartFile(
                "file",
                "users.txt",
                "text/plain",
                "some data".getBytes()
        );

        assertThrows(
                InvalidCsvFileException.class,
                () -> service.csvParse(file)
        );

        verifyNoInteractions(
                auditService,
                validateRecordService,
                csvRecordProcessingService
        );
    }

    @Test
    void csvParse_shouldProcessValidCsv() {

        String csv =
                "firstName,lastName,zipcode,phone1,phone2,email,web\n"
                + "John,Doe,90210,1234567890,9876543210,"
                + "john@example.com,https://example.com\n";

        MultipartFile file = new MockMultipartFile(
                "file",
                "users.csv",
                "text/csv",
                csv.getBytes()
        );

        

        ProcessingAudit audit = new ProcessingAudit();

        audit.setFilename("users.csv");
        audit.setStartTime(LocalDateTime.now());

       
        audit.setEndTime(LocalDateTime.now());

        when(auditService.startAudit("users.csv"))
                .thenReturn(audit);

       
        when(validateRecordService.validateRecord(
                any(UserCsvRecord.class)))
                .thenReturn(Collections.emptySet());



        User user = new User();

        user.setFirstName("John");
        user.setLastName("Doe");

        CompletableFuture<User> future =
                CompletableFuture.completedFuture(user);

        when(csvRecordProcessingService.processRecord(
                any(),
                any(UserCsvRecord.class)))
                .thenReturn(future);

        when(csvRecordProcessingService.addValidUser(
                any()))
                .thenReturn(List.of(user));

      

        String result = service.csvParse(file);

        

        verify(auditService)
                .startAudit("users.csv");

        verify(validateRecordService)
                .validateRecord(any(UserCsvRecord.class));

        verify(csvRecordProcessingService)
                .processRecord(
                        any(),
                        any(UserCsvRecord.class)
                );

        verify(csvRecordProcessingService)
                .addValidUser(any());

        verify(auditService)
                .completeAudit(
                        audit,
                        1,
                        1
                );
    }
}