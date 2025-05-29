package org.example.tokenmanager.controller;

import org.example.tokenmanager.model.AuditLog;
import org.example.tokenmanager.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auditlogs")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@RequestParam String userId) {
        logger.info("Fetching audit logs for user '{}'", userId);
        List<AuditLog> logs = auditLogRepository.findByUserId(userId);
        return ResponseEntity.ok(logs);
    }
}
