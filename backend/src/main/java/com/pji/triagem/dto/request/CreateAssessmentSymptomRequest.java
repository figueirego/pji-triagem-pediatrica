package com.pji.triagem.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateAssessmentSymptomRequest {

    @NotNull(message = "Sintoma é obrigatório")
    private Long symptomId;
    private List<CreateAssessmentAnswerRequest> answers = new ArrayList<>();
}
