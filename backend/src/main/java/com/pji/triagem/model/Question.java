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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pergunta")
@Getter
@Setter
@NoArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sintoma_id", nullable = false)
    private Symptom symptom;

    @Column(name = "codigo", nullable = false)
    private String code;

    @Column(name = "texto", nullable = false)
    private String text;

    @Column(name = "sub")
    private String subtitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private QuestionType type;

    @Column(name = "ordem", nullable = false)
    private Integer order = 0;
}
