package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.base.service.ResponseService;
import com.pji.triagem.dto.request.LoginForm;
import com.pji.triagem.dto.request.RegisterForm;
import com.pji.triagem.dto.response.AuthResponse;
import com.pji.triagem.dto.response.TokenResponse;
import com.pji.triagem.dto.response.UserProfileResponse;
import com.pji.triagem.model.TypeUser;
import com.pji.triagem.service.AuthService;
import com.pji.triagem.service.CurrentUserService;
import com.pji.triagem.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final ResponseService responseService;
    private final AuthService authService;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<TokenResponse>> loginUser(@Valid @RequestBody LoginForm form) {
        return responseService.ok(authService.login(form.getLogin(), form.getPassword(), TypeUser.USER));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<UserProfileResponse>> me() {
        return responseService.ok(UserProfileResponse.from(currentUserService.getCurrentUser()));
    }


    @PostMapping("/register/user")
    public ResponseEntity<ResponseDTO<AuthResponse>> registerClient(@Valid @RequestBody RegisterForm form) {
        var user = userService.registerClientUser(form.getLogin(), form.getPassword(), TypeUser.USER, form.getEmail(), form.getName());
        var token = authService.login(form.getLogin(), form.getPassword(), TypeUser.USER);
        return responseService.created(AuthResponse.builder()
                .usuario(UserProfileResponse.from(user))
                .token(token)
                .build());
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<AuthResponse>> register(@Valid @RequestBody RegisterForm form) {
        return registerClient(form);
    }

}
