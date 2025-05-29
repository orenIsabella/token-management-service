package org.example.tokenmanager.service;

import org.example.tokenmanager.model.Token;
import org.example.tokenmanager.repository.TokenRepository;
import org.example.tokenmanager.util.TokenHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TokenServiceTest {

    private TokenRepository tokenRepository;
    private AuditService auditService;
    private TokenService tokenService;

    @BeforeEach
    public void setup() {
        tokenRepository = mock(TokenRepository.class);
        auditService = mock(AuditService.class);
        tokenService = new TokenService(tokenRepository, auditService);
    }

    @Test
    public void testGenerateTokenStoresHashedTokenAndReturnsRaw() {
        String userId = "testUser";
        String rawToken = tokenService.generateToken(userId).getToken();
        assertNotNull(rawToken);
        ArgumentCaptor<Token> tokenCaptor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        Token savedToken = tokenCaptor.getValue();
        assertNotEquals(rawToken, savedToken.getTokenValue()); // ensure it was hashed
        assertEquals(TokenHasher.hash(rawToken), savedToken.getTokenValue());
        assertEquals(userId, savedToken.getUserId());
        verify(auditService).logAction(eq("GENERATE"), anyString(), eq(userId));
    }

    @Test
    public void testRotateTokenSuccess() {
        String userId = "testUser";
        String oldRaw = "oldRawToken";
        String oldHashed = TokenHasher.hash(oldRaw);
        Token oldToken = new Token(userId, oldHashed);
        when(tokenRepository.findByTokenValue(oldHashed)).thenReturn(Optional.of(oldToken));
        String newRawToken = tokenService.rotateToken(oldRaw).getToken();
        assertNotNull(newRawToken);
        assertNotEquals(oldRaw, newRawToken);
        verify(tokenRepository, times(2)).save(any()); // one for old, one for new
        verify(auditService).logAction(eq("ROTATE_OLD"), eq(oldHashed), eq(userId));
        verify(auditService).logAction(eq("ROTATE_NEW"), anyString(), eq(userId));
    }

    @Test
    public void testRotateTokenFailsIfNotFound() {
        String rawToken = "invalidToken";
        String hashed = TokenHasher.hash(rawToken);
        when(tokenRepository.findByTokenValue(hashed)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tokenService.rotateToken(rawToken)
        );
        assertEquals("Token not found.", ex.getMessage());
    }

    @Test
    public void testInvalidateTokenSuccess() {
        String raw = "rawToken";
        String hashed = TokenHasher.hash(raw);
        Token token = new Token("user1", hashed);
        when(tokenRepository.findByTokenValue(hashed)).thenReturn(Optional.of(token));
        tokenService.invalidateToken(raw);
        assertFalse(token.isValidToken());
        verify(tokenRepository).save(token);
        verify(auditService).logAction(eq("INVALIDATE"), eq(hashed), eq("user1"));
    }

    @Test
    public void testInvalidateTokenFailsIfNotFound() {
        String raw = "missingToken";
        String hashed = TokenHasher.hash(raw);

        when(tokenRepository.findByTokenValue(hashed)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tokenService.invalidateToken(raw)
        );
        assertEquals("Token not found.", ex.getMessage());
    }

    @Test
    public void testListTokensReturnsEmptyForUnknownUser() {
        when(tokenRepository.findByUserId("noUser")).thenReturn(List.of());
        List<Token> result = tokenService.listTokens("noUser");
        assertTrue(result.isEmpty());
        verify(tokenRepository).findByUserId("noUser");
    }

    @Test
    public void testRotateTokenFailsIfAlreadyInvalid() {
        String userId = "user1";
        String raw = "rawOld";
        String hashed = TokenHasher.hash(raw);
        Token oldToken = new Token(userId, hashed);
        oldToken.setValidToken(false);
        when(tokenRepository.findByTokenValue(hashed)).thenReturn(Optional.of(oldToken));
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                tokenService.rotateToken(raw)
        );
        assertEquals("Token is already invalid.", ex.getMessage());
    }

    @Test
    public void testGenerateTokenCallsAuditLog() {
        String userId = "auditTest";
        tokenService.generateToken(userId);
        verify(auditService, times(1)).logAction(eq("GENERATE"), anyString(), eq(userId));
    }

    @Test
    public void testInvalidateTokenLogsErrorWhenNotFound() {
        String raw = "notFoundToken";
        String hashed = TokenHasher.hash(raw);
        when(tokenRepository.findByTokenValue(hashed)).thenReturn(Optional.empty());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                tokenService.invalidateToken(raw)
        );
        assertEquals("Token not found.", ex.getMessage());
        verify(tokenRepository).findByTokenValue(hashed);
    }

}
