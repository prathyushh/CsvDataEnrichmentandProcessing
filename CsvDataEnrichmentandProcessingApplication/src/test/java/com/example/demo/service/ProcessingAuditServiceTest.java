package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.entity.ProcessingAudit;
import com.example.demo.repository.ProcessingAuditRepository;


@ExtendWith(MockitoExtension.class)
class ProcessingAuditServiceTest {

    @Mock
    private ProcessingAuditRepository repository;

    @InjectMocks
    private ProcessingAuditService service;


    @Test
    void startAudit_shouldCreateAudit() {

        ProcessingAudit audit = new ProcessingAudit();

        when(repository.save(any(ProcessingAudit.class)))
                .thenReturn(audit);

        ProcessingAudit result =
                service.startAudit("users.csv");

        assertEquals("users.csv", result.getFilename());
        assertEquals("Processing", result.getStatus());
        assertNotNull(result.getStartTime());

        verify(repository).save(any(ProcessingAudit.class));
    }


    @Test
    void completeAudit_shouldCompleteAudit() {

        ProcessingAudit audit = new ProcessingAudit();

        service.completeAudit(audit, 422, 381);

        assertEquals("COMPLETED", audit.getStatus());
        assertEquals(422, audit.getTotalRecords());
        assertEquals(381, audit.getSuccessfulRecords());
        assertEquals(41, audit.getFailedRecords());
        assertNotNull(audit.getEndTime());

        verify(repository).save(audit);
    }


    @Test
    void failAudit_shouldMarkAuditAsFailed() {

        ProcessingAudit audit = new ProcessingAudit();

        service.failAudit(audit);

        assertEquals("FAILED", audit.getStatus());
        assertNotNull(audit.getEndTime());

        verify(repository).save(audit);
    }
}