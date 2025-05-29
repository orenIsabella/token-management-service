package org.example.tokenmanager.service;

import org.example.tokenmanager.model.AuditLog;
import org.example.tokenmanager.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);


    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String action, String tokenValue, String userId) {
        AuditLog log = new AuditLog(action, tokenValue, userId);
        auditLogRepository.save(log);
        logger.debug("Audit log created: action='{}', token='{}', user='{}'", action, tokenValue, userId);
    }
}
