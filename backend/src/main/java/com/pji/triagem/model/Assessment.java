package com.pji.triagem.model;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "avaliacao")
@Getter
@Setter
@NoArgsConstructor
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crianca_id", nullable = false)
    private Child child;

    @Enumerated(EnumType.STRING)
    @Column(name = "classificacao_final", nullable = false)
    private Classification finalClassification;

    @Column(name = "score_total", nullable = false)
    private Integer totalScore;

    @Column(name = "red_flag_detected", nullable = false)
    private Boolean redFlagDetected = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "respostas", columnDefinition = "jsonb")
    private String responses;

    @Column(name = "protocolo_versao", nullable = false)
    private String protocolVersion = "1.0.0";

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentSymptom> symptoms = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (protocolVersion == null || protocolVersion.isBlank()) {
            protocolVersion = "1.0.0";
        }
        if (redFlagDetected == null) {
            redFlagDetected = false;
        }
    }

    public void addSymptom(AssessmentSymptom symptom) {
        symptoms.add(symptom);
        symptom.setAssessment(this);
    }
}
