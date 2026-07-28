package com.pji.triagem.dto.response;

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
public class QuestionnaireSymptomResponse {

    private Long symptomId;
    private String code;
    private String name;

    @Builder.Default
    private List<QuestionResponse> questions = new ArrayList<>();
}
