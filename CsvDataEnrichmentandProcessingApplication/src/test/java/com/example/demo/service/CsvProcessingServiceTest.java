package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.UserCsvRecord;
import com.example.demo.entity.Address;
import com.example.demo.entity.ProcessingAudit;
import com.example.demo.exception.InvalidCsvFileException;
import com.example.demo.repository.UserRepository;

import jakarta.validation.Validator;

@ExtendWith(MockitoExtension.class)
class CsvProcessingServiceTest {

    @Mock
    private Validator validator;

    @Mock
    private UserRepository repository;

    @Mock
    private AddressEnrichmentService addressService;

    @Mock
    private ProcessingAuditService auditService;

    private CsvProcessingService service;

    private Executor executor;

    @BeforeEach
    void setUp() {

        executor = Runnable::run;

        service = new CsvProcessingService(
                validator,
                repository,
                addressService,
                executor,
                auditService
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
                repository,
                addressService,
                auditService
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
                repository,
                addressService,
                auditService
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

      
        when(validator.validate(any(UserCsvRecord.class)))
                .thenReturn(Collections.emptySet());

        
        ProcessingAudit audit = new ProcessingAudit();

        audit.setFilename("users.csv");
        audit.setStartTime(LocalDateTime.now());

        
        when(auditService.startAudit("users.csv"))
                .thenReturn(audit);

        
        Address address = new Address();
        address.setZipCode("90210");

       
        when(addressService.addressSearchByApi("90210"))
                .thenReturn(address);

       
        doAnswer(invocation -> {

            ProcessingAudit processingAudit =
                    invocation.getArgument(0);

            processingAudit.setStatus("COMPLETED");
            processingAudit.setEndTime(LocalDateTime.now());
            processingAudit.setTotalRecords(1);
            processingAudit.setSuccessfulRecords(1);
            processingAudit.setFailedRecords(0);

            return null;

        }).when(auditService)
          .completeAudit(audit, 1, 1);

        
        service.csvParse(file);

       
        verify(auditService)
                .startAudit("users.csv");

       
        verify(addressService)
                .addressSearchByApi("90210");

        
        verify(repository)
                .saveAll(any());

        
        verify(auditService)
                .completeAudit(
                        audit,
                        1,
                        1
                );
    }
}