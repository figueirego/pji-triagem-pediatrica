package com.pji.triagem.mapper;

import com.pji.triagem.dto.response.AssessmentResultResponse;
import com.pji.triagem.dto.response.AssessmentSymptomResultResponse;
import com.pji.triagem.model.Assessment;
import com.pji.triagem.model.AssessmentSymptom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssessmentMapper {

    @Mapping(target = "assessmentId", source = "id")
    @Mapping(target = "childId", source = "child.id")
    @Mapping(target = "symptoms", ignore = true)
    AssessmentResultResponse toResultResponse(Assessment assessment);

    @Mapping(target = "symptomId", source = "symptom.id")
    @Mapping(target = "symptomName", source = "symptom.name")
    AssessmentSymptomResultResponse toSymptomResultResponse(AssessmentSymptom assessmentSymptom);
}
