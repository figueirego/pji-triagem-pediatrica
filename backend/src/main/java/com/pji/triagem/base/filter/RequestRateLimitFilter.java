package com.pji.triagem.base.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

/** Per-instance protection. A trusted edge must enforce a shared limit when scaling. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RequestRateLimitFilter extends OncePerRequestFilter {
    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_CLIENTS = 10_000;
    private final int authLimit;
    private final int generalLimit;
    private final Clock clock;
    private final Map<String, Window> clients = new HashMap<>();

    private record Window(long start, int total, int auth) { }

    @Autowired
    public RequestRateLimitFilter(@Value("${app.rate-limit.auth-per-minute:20}") int authLimit,
                                  @Value("${app.rate-limit.requests-per-minute:240}") int generalLimit) {
        this(authLimit, generalLimit, Clock.systemUTC());
    }

    RequestRateLimitFilter(int authLimit, int generalLimit, Clock clock) {
        if (authLimit < 1 || generalLimit < 1) throw new IllegalArgumentException("Rate limits must be positive");
        this.authLimit = authLimit;
        this.generalLimit = generalLimit;
        this.clock = clock;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        boolean authentication = request.getRequestURI().startsWith("/auth/")
                || "/usuarios".equals(request.getRequestURI());
        // Do not trust a caller-controlled X-Forwarded-For header.
        if (!allow(request.getRemoteAddr(), authentication)) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":429,\"mensagem\":\"Muitas solicitações. Aguarde um minuto e tente novamente.\",\"campos\":[]}");
            return;
        }
        chain.doFilter(request, response);
    }

    private synchronized boolean allow(String address, boolean authentication) {
        long now = clock.millis();
        Window previous = clients.get(address);
        if (previous == null || now - previous.start() >= WINDOW_MILLIS) {
            clients.entrySet().removeIf(entry -> now - entry.getValue().start() >= WINDOW_MILLIS);
            if (clients.size() >= MAX_CLIENTS) return false;
            previous = new Window(now, 0, 0);
        }
        if (previous.total() >= generalLimit || (authentication && previous.auth() >= authLimit)) return false;
        clients.put(address, new Window(previous.start(), previous.total() + 1,
                previous.auth() + (authentication ? 1 : 0)));
        return true;
    }
}
