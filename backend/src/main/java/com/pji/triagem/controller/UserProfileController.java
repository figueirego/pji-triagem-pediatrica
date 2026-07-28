package com.pji.triagem.controller;

import com.pji.triagem.base.dto.ResponseDTO;
import com.pji.triagem.base.service.ResponseService;
import com.pji.triagem.dto.request.UpdateUserProfileRequest;
import com.pji.triagem.dto.response.UserProfileResponse;
import com.pji.triagem.service.CurrentUserService;
import com.pji.triagem.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@AllArgsConstructor
public class UserProfileController {

    private final ResponseService responseService;
    private final CurrentUserService currentUserService;
    private final UserService userService;

    @PatchMapping("/me")
    public ResponseEntity<ResponseDTO<UserProfileResponse>> updateMe(
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        Long userId = currentUserService.getCurrentUser().getId();
        return responseService.ok(UserProfileResponse.from(userService.updateProfile(userId, request)));
    }
}
