package com.pji.triagem.base.filter;

import com.pji.triagem.base.service.JwtAuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityFilterTest {
    private final JwtAuthenticationService authentication = mock(JwtAuthenticationService.class);
    private final SecurityFilter filter = new SecurityFilter(authentication);
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();
    private final FilterChain chain = mock(FilterChain.class);

    @AfterEach void clearContext() { SecurityContextHolder.clearContext(); }

    @Test void downstreamErrorsAreNotMisreportedAsExpiredTokens() throws Exception {
        when(authentication.getTokenFromRequest(request)).thenReturn("access-token");
        when(authentication.getAuthentication("access-token")).thenReturn(
                new UsernamePasswordAuthenticationToken("user", null, List.of()));
        doThrow(new ServletException("database unavailable")).when(chain).doFilter(request, response);
        assertThrows(ServletException.class, () -> filter.doFilter(request, response, chain));
        assertNotEquals(401, response.getStatus());
    }

    @Test void invalidBearerDoesNotReachProtectedController() throws Exception {
        when(authentication.getTokenFromRequest(request)).thenReturn("invalid-token");
        when(authentication.getAuthentication("invalid-token")).thenReturn(null);
        filter.doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        verifyNoInteractions(chain);
    }

    @Test void missingBearerContinuesToAuthorizationLayer() throws Exception {
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}
