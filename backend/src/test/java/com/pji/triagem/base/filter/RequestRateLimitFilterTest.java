package com.pji.triagem.base.filter;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.junit.jupiter.api.Assertions.*;

class RequestRateLimitFilterTest {
    private final RequestRateLimitFilter filter = new RequestRateLimitFilter(2, 4,
            Clock.fixed(Instant.parse("2026-09-24T12:00:00Z"), ZoneOffset.UTC));

    private MockHttpServletResponse request(String uri, String address) throws Exception {
        var request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr(address);
        request.addHeader("X-Forwarded-For", "different-on-every-request");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }

    @Test void authAliasesShareLimitAndClientCannotBypassWithProxyHeader() throws Exception {
        assertEquals(200, request("/auth/login", "127.0.0.1").getStatus());
        assertEquals(200, request("/usuarios", "127.0.0.1").getStatus());
        var blocked = request("/auth/register/user", "127.0.0.1");
        assertEquals(429, blocked.getStatus());
        assertNotNull(blocked.getHeader("Retry-After"));
        assertEquals(200, request("/auth/login", "127.0.0.2").getStatus());
    }

    @Test void allEndpointsHaveAnIndependentGeneralLimit() throws Exception {
        for (int i = 0; i < 4; i++) assertEquals(200, request("/assessments", "127.0.0.1").getStatus());
        assertEquals(429, request("/assessments", "127.0.0.1").getStatus());
    }
}
