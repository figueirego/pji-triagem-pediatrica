package com.pji.triagem.mapper;

import com.pji.triagem.dto.response.QuestionResponse;
import com.pji.triagem.model.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuestionMapper {

    @Mapping(target = "options", ignore = true)
    QuestionResponse toResponse(Question question);
}
