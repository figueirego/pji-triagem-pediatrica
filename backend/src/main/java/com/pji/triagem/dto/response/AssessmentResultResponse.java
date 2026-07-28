package com.pji.triagem.dto.response;

import com.pji.triagem.model.Classification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentResultResponse {

    private Long assessmentId;
    private Long childId;
    private Classification finalClassification;
    private Integer totalScore;
    private Boolean redFlagDetected;

    @Builder.Default
    private List<AssessmentSymptomResultResponse> symptoms = new ArrayList<>();
}
