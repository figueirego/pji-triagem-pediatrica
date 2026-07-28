package com.pji.triagem.mapper;

import com.pji.triagem.dto.response.OrientationItemResponse;
import com.pji.triagem.dto.response.OrientationSymptomResponse;
import com.pji.triagem.model.AssessmentSymptom;
import com.pji.triagem.model.Orientation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrientationMapper {

    @Mapping(target = "symptomId", source = "symptom.id")
    @Mapping(target = "symptomName", source = "symptom.name")
    OrientationItemResponse toItemResponse(Orientation orientation);

    @Mapping(target = "symptomId", source = "symptom.id")
    @Mapping(target = "symptomName", source = "symptom.name")
    OrientationSymptomResponse toSymptomResponse(AssessmentSymptom assessmentSymptom);
}
