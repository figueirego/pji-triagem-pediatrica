package com.pji.triagem.mapper;

import com.pji.triagem.dto.request.CreateChildRequest;
import com.pji.triagem.dto.response.ChildHomeResponse;
import com.pji.triagem.dto.response.ChildResponse;
import com.pji.triagem.model.Child;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChildMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Child toEntity(CreateChildRequest request);

    @Mapping(target = "age", ignore = true)
    @Mapping(target = "ageInMonths", ignore = true)
    ChildResponse toResponse(Child child);

    @Mapping(target = "age", ignore = true)
    @Mapping(target = "ageInMonths", ignore = true)
    @Mapping(target = "totalAssessments", ignore = true)
    @Mapping(target = "lastAssessmentAt", ignore = true)
    ChildHomeResponse toHomeResponse(Child child);
}
