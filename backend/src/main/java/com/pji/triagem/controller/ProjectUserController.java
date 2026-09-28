package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.dto.request.RegisterForm;
import com.pji.triagem.dto.response.AuthResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Registration alias from the project API example, sharing the existing validation. */
@RestController
@AllArgsConstructor
public class ProjectUserController {
    private final AuthController authController;

    @PostMapping("/usuarios")
    public ResponseEntity<ResponseDTO<AuthResponse>> register(@Valid @RequestBody RegisterForm form) {
        return authController.register(form);
    }
}
