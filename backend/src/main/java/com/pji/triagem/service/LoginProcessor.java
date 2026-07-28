package com.pji.triagem.service;

import com.pji.triagem.dto.response.InfoLoginDTO;
import com.pji.triagem.dto.response.TokenPair;
import com.pji.triagem.dto.response.UserAuth;
import com.pji.triagem.model.TypeUser;

public interface LoginProcessor {
    /**
     * Resolve o nome do usuário para incluir no token.
     */
    InfoLoginDTO resolveName(UserAuth userAuth);

    /**
     * Gera um par de tokens (access e refresh) para o usuário.
     */
    TokenPair generateToken(UserAuth userAuth, InfoLoginDTO infoLoginDTO);

    /**
     * Gera um novo par de tokens baseado em um refresh token válido.
     */
    TokenPair refreshToken(UserAuth userAuth, String name);

    /**
     * Informa qual é o tipo de usuário que esse processor atende.
     */
    TypeUser getTypeUser();
}