package com.pji.triagem.service;

import com.pji.triagem.base.service.BaseService;
import com.pji.triagem.dto.request.UpdateUserProfileRequest;
import com.pji.triagem.dto.response.UserAuth;
import com.pji.triagem.model.TypeUser;
import com.pji.triagem.model.User;

public interface UserService extends BaseService<User> {

    User findByLoginAndType(String login, TypeUser typeUser);

    void resetLoginAttempts(String login, TypeUser typeUser);

    void addLoginAttemps(String login, TypeUser typeUser);

    UserAuth loadUserById(Long userId);

    User registerClientUser(String login, String password, TypeUser typeUser, String email, String name);

    User updateProfile(Long userId, UpdateUserProfileRequest request);
}
