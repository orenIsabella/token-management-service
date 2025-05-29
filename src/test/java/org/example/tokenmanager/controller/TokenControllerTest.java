package org.example.tokenmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tokenmanager.controller.dto.TokenResponse;
import org.example.tokenmanager.model.Token;
import org.example.tokenmanager.service.TokenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TokenController.class)
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenService tokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGenerateToken() throws Exception {
        TokenResponse mockResponse = new TokenResponse("someToken", "user1");
        when(tokenService.generateToken("user1")).thenReturn(mockResponse);

        mockMvc.perform(post("/tokens")
                        .param("userId", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("someToken"))
                .andExpect(jsonPath("$.userId").value("user1"));
    }

    @Test
    public void testRotateToken() throws Exception {
        String oldToken = "oldToken";
        TokenResponse newTokenResponse = new TokenResponse("newToken", "user1");

        when(tokenService.rotateToken(oldToken)).thenReturn(newTokenResponse);

        mockMvc.perform(post("/tokens/rotate")
                        .param("oldToken", oldToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("newToken"))
                .andExpect(jsonPath("$.userId").value("user1"));
    }


    @Test
    public void testInvalidateToken() throws Exception {
        doNothing().when(tokenService).invalidateToken("t123");

        mockMvc.perform(delete("/tokens/t123"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testListTokens() throws Exception {
        Token token1 = new Token("user1", "token1");
        Token token2 = new Token("user1", "token2");

        when(tokenService.listTokens("user1")).thenReturn(List.of(token1, token2));

        mockMvc.perform(get("/tokens")
                        .param("userId", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
