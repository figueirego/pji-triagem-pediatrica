package com.pji.triagem.mapper;

import com.pji.triagem.dto.response.QuestionnaireSymptomResponse;
import com.pji.triagem.dto.response.SymptomResponse;
import com.pji.triagem.model.Symptom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SymptomMapper {

    SymptomResponse toResponse(Symptom symptom);

    @Mapping(target = "symptomId", source = "id")
    @Mapping(target = "questions", ignore = true)
    QuestionnaireSymptomResponse toQuestionnaireSymptomResponse(Symptom symptom);
}
