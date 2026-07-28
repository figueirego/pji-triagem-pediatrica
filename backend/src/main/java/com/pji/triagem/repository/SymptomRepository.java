package com.pji.triagem.repository;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.model.Symptom;

import java.util.List;

public interface SymptomRepository extends BaseRepository<Symptom, Long> {

    List<Symptom> findAllByOrderByOrderAsc();

    List<Symptom> findAllByIdIn(List<Long> symptomIds);
}
