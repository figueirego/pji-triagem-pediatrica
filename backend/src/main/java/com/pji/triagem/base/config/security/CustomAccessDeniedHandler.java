package com.pji.triagem.base.config.security;

import com.pji.triagem.base.service.impl.MessageServiceImpl;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final MessageServiceImpl messageService;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, java.io.IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String errorMessage = "{\"status\":401,\"mensagem\":\"Token ausente ou inválido\",\"campos\":[]}";
        response.getWriter().write(errorMessage);
    }
}
