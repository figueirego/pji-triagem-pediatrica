package com.pji.triagem.service;

import com.pji.triagem.model.User;

public interface CurrentUserService {

    User getCurrentUser();

    void assertCanAccessUser(Long userId);

    boolean isAdmin(User user);
}
