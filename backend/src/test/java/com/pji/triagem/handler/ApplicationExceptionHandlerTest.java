package com.pji.triagem.handler;

import com.pji.triagem.dto.response.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationExceptionHandlerTest {
    @Test void invalidTokenReturns401WithoutExposingExceptionDetails() throws Exception {
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .standaloneSetup(new InvalidTokenController())
                .setControllerAdvice(new ApplicationExceptionHandler()).build();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/test/invalid-token"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.mensagem")
                        .value("Token inválido ou expirado"));
    }

    @org.springframework.web.bind.annotation.RestController
    static class InvalidTokenController {
        @org.springframework.web.bind.annotation.PostMapping("/test/invalid-token")
        public void refresh() {
            throw new com.pji.triagem.exception.InvalidTokenException("Internal JWT claims: private token data");
        }
    }

    @Test void unexpectedErrorDoesNotExposeDatabaseDetails() {
        var response = new ApplicationExceptionHandler().handleRuntimeException(
                new RuntimeException("jdbc:postgresql private patient data"),
                new ServletWebRequest(new MockHttpServletRequest()));
        assertEquals(500, response.getStatusCode().value());
        assertFalse(((ApiError) response.getBody()).getMensagem().contains("patient"));
    }
}
