package com.pji.triagem.base.config.security;

import com.pji.triagem.base.service.impl.MessageServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private MessageServiceImpl messageService;

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException e)
            throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        String errorMessage = "{\"status\":401,\"mensagem\":\"Token ausente ou inválido\",\"campos\":[]}";
        res.getWriter().write(errorMessage);
    }
}
