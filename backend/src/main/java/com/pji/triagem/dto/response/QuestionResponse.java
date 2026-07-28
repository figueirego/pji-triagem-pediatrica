package com.pji.triagem.dto.response;

import com.pji.triagem.model.QuestionType;
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
public class QuestionResponse {

    private Long id;
    private String code;
    private String text;
    private String subtitle;
    private QuestionType type;
    private Integer order;

    @Builder.Default
    private List<QuestionOptionResponse> options = new ArrayList<>();
}
