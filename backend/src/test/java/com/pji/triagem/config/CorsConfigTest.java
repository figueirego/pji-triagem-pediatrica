package com.pji.triagem.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {
    @Test void browserOriginMustBeConfigured() throws Exception {
        var filter = new CorsConfig().corsFilter();
        for (String origin : new String[]{"http://localhost:8082", "https://untrusted.example"}) {
            var request = new MockHttpServletRequest("OPTIONS", "/symptoms");
            request.addHeader("Origin", origin);
            request.addHeader("Access-Control-Request-Method", "GET");
            var response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            if (origin.contains("localhost")) {
                assertEquals(origin, response.getHeader("Access-Control-Allow-Origin"));
            } else {
                assertEquals(403, response.getStatus());
                assertNull(response.getHeader("Access-Control-Allow-Origin"));
            }
        }
    }
}
