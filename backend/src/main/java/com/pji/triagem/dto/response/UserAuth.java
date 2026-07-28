package com.pji.triagem.dto.response;

import com.pji.triagem.model.TypeUser;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
public class UserAuth extends User {

    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private TypeUser typeUser;
    private String login;
    private Integer attemptsCount;
    private Boolean isBlockedTemporary;
    private Boolean isActive;
    private String name;

    public UserAuth(String username, String password, Collection<? extends GrantedAuthority> authorities, Long id, TypeUser typeUser,
                    Integer attemptsCount, Boolean isBlockedTemporary, String login, Boolean isActive, String name) {

        super(username, password, authorities);
        this.id = id;
        this.typeUser = typeUser;
        this.attemptsCount = attemptsCount;
        this.isBlockedTemporary = isBlockedTemporary;
        this.login = login;
        this.isActive = isActive;
        this.name = name;
    }

    public static UserAuth fromEntity(com.pji.triagem.model.User userEntity) {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(userEntity.getType().toString()));

        return new UserAuth(
                userEntity.getCpf(),
                userEntity.getPassword(),
                authorities,
                userEntity.getId(),
                userEntity.getType(),
                userEntity.getAttemptsCount(),
                userEntity.getIsBlockedTemporary(),
                userEntity.getCpf(),
                userEntity.getIsActive(),
                userEntity.getName()

        );
    }
}