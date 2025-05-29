package org.example.tokenmanager.controller;

import org.example.tokenmanager.controller.dto.TokenResponse;
import org.example.tokenmanager.model.Token;
import org.example.tokenmanager.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tokens")
public class TokenController {

    private final TokenService tokenService;
    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<TokenResponse> generateToken(@RequestParam String userId) {
        logger.info("Received request: Generate token for user '{}'", userId);
        return ResponseEntity.ok(tokenService.generateToken(userId));
    }

    @PostMapping("/rotate")
    public ResponseEntity<TokenResponse> rotateToken(@RequestParam String oldToken) {
        logger.info("Received request: Rotate token '{}'", oldToken);
        return ResponseEntity.ok(tokenService.rotateToken(oldToken));
    }

    @GetMapping
    public ResponseEntity<List<Token>> listTokens(@RequestParam String userId) {
        logger.info("Received request: List tokens for user '{}'", userId);
        return ResponseEntity.ok(tokenService.listTokens(userId));
    }

    @DeleteMapping("/{tokenValue}")
    public ResponseEntity<Void> invalidateToken(@PathVariable String tokenValue) {
        logger.info("Received request: Invalidate token '{}'", tokenValue);
        tokenService.invalidateToken(tokenValue);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<Token>> listActiveTokens(@RequestParam String userId) {
        logger.info("Received request: List ACTIVE tokens for user '{}'", userId);
        return ResponseEntity.ok(tokenService.listActiveTokens(userId));
    }

}
