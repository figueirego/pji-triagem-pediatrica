package com.pji.triagem.dto.response;

import com.pji.triagem.model.Classification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrientationSymptomResponse {

    private Long symptomId;
    private String symptomName;
    private Classification classification;
    private Integer score;
    private Boolean redFlagDetected;
}
