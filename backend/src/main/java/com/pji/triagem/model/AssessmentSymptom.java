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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao_sintoma")
@Getter
@Setter
@NoArgsConstructor
public class AssessmentSymptom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sintoma_id", nullable = false)
    private Symptom symptom;

    @Enumerated(EnumType.STRING)
    @Column(name = "classificacao", nullable = false)
    private Classification classification;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "red_flag_detected", nullable = false)
    private Boolean redFlagDetected = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "respostas", nullable = false, columnDefinition = "jsonb")
    private String responses;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (redFlagDetected == null) {
            redFlagDetected = false;
        }
    }
}
