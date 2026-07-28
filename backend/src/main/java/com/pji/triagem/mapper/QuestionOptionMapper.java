package com.pji.triagem.mapper;

import com.pji.triagem.dto.response.QuestionOptionResponse;
import com.pji.triagem.model.QuestionOption;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionOptionMapper {

    QuestionOptionResponse toResponse(QuestionOption option);
}
