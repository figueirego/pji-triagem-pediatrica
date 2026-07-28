package com.pji.triagem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "peso_yesno")
@Getter
@Setter
@NoArgsConstructor
public class YesNoWeight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pergunta_id", nullable = false, unique = true)
    private Question question;

    @Column(name = "score_yes", nullable = false)
    private Integer yesScore;

    @Column(name = "score_no", nullable = false)
    private Integer noScore;

    @Column(name = "score_dunno", nullable = false)
    private Integer unknownScore;

    @Column(name = "red_flag_on_yes", nullable = false)
    private Boolean redFlagOnYes = false;

    @Column(name = "red_flag_on_no", nullable = false)
    private Boolean redFlagOnNo = false;
}
