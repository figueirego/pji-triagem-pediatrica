package com.pji.triagem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "opcao_pergunta")
@Getter
@Setter
@NoArgsConstructor
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pergunta_id", nullable = false)
    private Question question;

    @Column(name = "codigo", nullable = false)
    private String code;

    @Column(name = "texto", nullable = false)
    private String text;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "red_flag", nullable = false)
    private Boolean redFlag = false;

    @Column(name = "ordem", nullable = false)
    private Integer order = 0;
}
