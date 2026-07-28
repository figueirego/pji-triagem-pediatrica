package com.pji.triagem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateAssessmentRequest {

    @NotNull(message = "Criança é obrigatória")
    private Long childId;

    @NotEmpty(message = "A lista de sintomas é obrigatória")
    private List<CreateAssessmentSymptomRequest> symptoms = new ArrayList<>();
}
