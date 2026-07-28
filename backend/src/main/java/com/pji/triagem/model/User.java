package com.pji.triagem.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false)
    private String name;
    
    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false)
    private TypeUser type;

    @Column(name = "senha" , nullable = false)
    private String password;  // Senha criptografada com BCrypt

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "atualizado_em")
    private LocalDateTime updatedAt;

    @Column(nullable = false, name = "ativo")
    private Boolean isActive = true;  // Ativo ou não

    @Column(nullable = false, name = "bloqueado")
    private Boolean isBlocked = false;  // Bloqueado por tentativas falhas


    @Column(nullable = false, name = "bloqueado_temporariamente")
    private Boolean isBlockedTemporary = false;  // Bloqueado ate redefinir senha

    @Column(nullable = false, name = "tentativas")
    private Integer attemptsCount = 0;  // Número de tentativas de login falhas

    public User(String cpf, String encode, TypeUser typeUser, String email, String name) {
        this.cpf = cpf;
        this.password = encode;
        this.type = typeUser;
        this.createdAt = LocalDateTime.now();
        this.name = name;
        this.email = email;
    }

    public User(String login, String encode, TypeUser type, TypeUser typeUser) {
    }
}
