package com.pji.triagem.repository;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.model.YesNoWeight;

import java.util.Optional;

public interface YesNoWeightRepository extends BaseRepository<YesNoWeight, Long> {

    Optional<YesNoWeight> findByQuestionId(Long questionId);
}
