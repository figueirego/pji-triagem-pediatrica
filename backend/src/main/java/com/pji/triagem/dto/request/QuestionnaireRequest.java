package com.pji.triagem.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionnaireRequest {

    @NotEmpty(message = "A lista de sintomas é obrigatória")
    private List<Long> symptomIds = new ArrayList<>();
}
