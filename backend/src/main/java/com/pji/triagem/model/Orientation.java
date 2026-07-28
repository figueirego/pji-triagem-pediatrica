package com.pji.triagem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "orientacao")
@Getter
@Setter
@NoArgsConstructor
public class Orientation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sintoma_id")
    private Symptom symptom;

    @Enumerated(EnumType.STRING)
    @Column(name = "classificacao", nullable = false)
    private Classification classification;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private OrientationType type;

    @Column(name = "titulo", nullable = false)
    private String title;

    @Column(name = "descricao", nullable = false, length = 1500)
    private String description;

    @Column(name = "ordem", nullable = false)
    private Integer order = 0;

    @Column(name = "ativo", nullable = false)
    private Boolean active = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (order == null) {
            order = 0;
        }
        if (active == null) {
            active = true;
        }
    }
}
