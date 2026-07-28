package com.pji.triagem.dto.request;

import com.pji.triagem.model.YesNoAnswer;
import lombok.Data;

@Data
public class CreateAssessmentAnswerRequest {

    private Long questionId;
    private Long optionId;
    private YesNoAnswer answer;
}
