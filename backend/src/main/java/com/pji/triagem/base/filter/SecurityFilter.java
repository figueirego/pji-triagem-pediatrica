package com.pji.triagem.base.filter;

import com.pji.triagem.base.service.JwtAuthenticationService;
import com.pji.triagem.exception.BlockedUserException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@AllArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final JwtAuthenticationService jwtAuthService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = jwtAuthService.getTokenFromRequest(request);

        // Se não houver token, prossiga sem bloquear a requisição
        if (token == null) {
             filterChain.doFilter(request, response);
             return;
        }

        try {
            Authentication authentication = jwtAuthService.getAuthentication(token);

            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);

        } catch (BlockedUserException blockedUserException) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":403,\"mensagem\":\"" + blockedUserException.getMessage() + "\",\"campos\":[]}");
            response.getWriter().flush();
            return;
        } catch (Exception e) {
            // Deixe o AuthenticationEntryPoint lidar com a resposta 401
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":401,\"mensagem\":\"Token inválido ou expirado\",\"campos\":[]}");
            response.getWriter().flush();
            return;
        }
    }
}
