package com.pji.triagem.mapper;

import com.pji.triagem.dto.response.AssessmentHistoryChildResponse;
import com.pji.triagem.dto.response.AssessmentHistoryItemResponse;
import com.pji.triagem.model.Assessment;
import com.pji.triagem.model.Child;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssessmentHistoryMapper {

    AssessmentHistoryChildResponse toChildResponse(Child child);

    @Mapping(target = "childId", source = "child.id")
    @Mapping(target = "childName", source = "child.name")
    @Mapping(target = "classification", source = "finalClassification")
    @Mapping(target = "symptoms", ignore = true)
    AssessmentHistoryItemResponse toItemResponse(Assessment assessment);
}
