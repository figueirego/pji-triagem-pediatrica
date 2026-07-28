package com.pji.triagem.dto.response;

import com.pji.triagem.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String login;
    private String email;
    private String name;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .login(user.getCpf())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
