package com.pji.triagem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sintoma")
@Getter
@Setter
@NoArgsConstructor
public class Symptom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true)
    private String code;

    @Column(name = "nome", nullable = false)
    private String name;

    @Column(name = "descricao_curta")
    private String shortDescription;

    @Column(name = "icone_ref")
    private String iconRef;

    @Column(name = "cor_hex")
    private String colorHex;

    @Column(name = "ordem", nullable = false)
    private Integer order = 0;
}
