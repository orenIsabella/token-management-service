package org.example.tokenmanager.service;

import org.example.tokenmanager.controller.dto.TokenResponse;
import org.example.tokenmanager.model.Token;
import org.example.tokenmanager.repository.TokenRepository;
import org.example.tokenmanager.util.TokenHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final AuditService auditService;
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    public TokenService(TokenRepository tokenRepository, AuditService auditService) {
        this.tokenRepository = tokenRepository;
        this.auditService = auditService;
    }

    public TokenResponse generateToken(String userId) {
        logger.info("Generating token for user '{}'", userId);
        String rawToken = generateSecureToken();
        String hashed = TokenHasher.hash(rawToken);
        Token token = new Token(userId, hashed);
        tokenRepository.save(token);
        auditService.logAction("GENERATE", hashed, userId);
        logger.debug("Generated token (raw): {}", rawToken);
        return new TokenResponse(rawToken, userId);
    }

    public TokenResponse rotateToken(String oldTokenValue) {
        String hashedOld = TokenHasher.hash(oldTokenValue);
        Optional<Token> existingTokenOpt = tokenRepository.findByTokenValue(hashedOld);
        if (existingTokenOpt.isEmpty()) {
            logger.error("Token rotation failed: token not found - {}", oldTokenValue);
            throw new IllegalArgumentException("Token not found.");
        }
        Token oldToken = existingTokenOpt.get();
        if (!oldToken.isValidToken()) {
            logger.warn("Token already invalid: {}", oldTokenValue);
            throw new IllegalStateException("Token is already invalid.");
        }
        oldToken.setValidToken(false);
        oldToken.setRotatedAt(LocalDateTime.now());
        tokenRepository.save(oldToken);
        String newRawToken = generateSecureToken();
        String newHashedToken = TokenHasher.hash(newRawToken);
        Token newToken = new Token(oldToken.getUserId(), newHashedToken);
        tokenRepository.save(newToken);
        logger.info("Token rotated for user '{}'.", oldToken.getUserId());
        auditService.logAction("ROTATE_OLD", hashedOld, oldToken.getUserId());
        auditService.logAction("ROTATE_NEW", newHashedToken, oldToken.getUserId());
        return new TokenResponse(newRawToken, oldToken.getUserId());
    }

    public void invalidateToken(String tokenValue) {
        String hashed = TokenHasher.hash(tokenValue);
        logger.info("Attempting to invalidate token: {}", tokenValue);
        Optional<Token> tokenOpt = tokenRepository.findByTokenValue(hashed);
        if (tokenOpt.isPresent()) {
            Token token = tokenOpt.get();
            token.setValidToken(false);
            tokenRepository.save(token);
            logger.info("Token invalidated for user '{}'.", token.getUserId());
            auditService.logAction("INVALIDATE", hashed, token.getUserId());
        } else {
            logger.error("Token invalidation failed: token not found - {}", tokenValue);
            throw new IllegalArgumentException("Token not found.");
        }
    }

    public List<Token> listTokens(String userId) {
        logger.info("Listing tokens for user '{}'", userId);
        return tokenRepository.findByUserId(userId);
    }

    public List<Token> listActiveTokens(String userId) {
        logger.info("Listing active tokens for user '{}'", userId);
        return tokenRepository.findByUserIdAndValidTokenTrue(userId);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
