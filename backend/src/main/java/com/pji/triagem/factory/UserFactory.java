package com.pji.triagem.factory;

import com.pji.triagem.model.TypeUser;
import com.pji.triagem.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Classe responsável por criar objetos User
 */
public class UserFactory {

    private UserFactory(){}

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Método responsavel por criar um usuário de tipo adesao
     * @param login - login do usuário
     * @param password - Senha do usuário
     * @return - usuário criado
     */
    public static User createUserTypeClient(String login, String password, String email, String name) {
        return new User(login, passwordEncoder.encode(password), TypeUser.USER, email, name);
    }


    public static User createUserTypeDoctor(String login, String password, TypeUser type) {
        return new User(login, passwordEncoder.encode(password), type, TypeUser.DOCTOR);
    }
}
