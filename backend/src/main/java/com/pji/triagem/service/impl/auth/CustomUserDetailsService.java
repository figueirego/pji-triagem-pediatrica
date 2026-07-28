package com.pji.triagem.service.impl.auth;

import com.pji.triagem.dto.response.UserAuth;
import com.pji.triagem.exception.BlockedUserException;
import com.pji.triagem.model.TypeUser;
import com.pji.triagem.model.User;
import com.pji.triagem.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UsernameNotFoundException("Este método deve ser chamado com o tipo de usuário");
    }

    public UserAuth loadUserByUsernameAndType(String username, TypeUser typeUser) throws UsernameNotFoundException {
     User user = userService.findByLoginAndType(username, typeUser);
     if(Boolean.TRUE.equals(user.getIsBlocked())){
            throw new BlockedUserException("Seu usuário foi bloqueado por excesso de tentativas de login, por favor altere sua senha");
     }

        return new UserAuth(user.getCpf(), user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getType().toString())),
                user.getId(), typeUser, user.getAttemptsCount(), user.getIsBlockedTemporary(), user.getCpf(), user.getIsActive(), user.getName());
    }
}
