package com.pji.triagem.repository;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.model.Classification;
import com.pji.triagem.model.Orientation;

import java.util.List;

public interface OrientationRepository extends BaseRepository<Orientation, Long> {

    List<Orientation> findByActiveTrueAndClassificationAndSymptomIsNullOrderByOrderAsc(Classification classification);

    List<Orientation> findByActiveTrueAndClassificationAndSymptomIdInOrderByOrderAsc(
            Classification classification,
            List<Long> symptomIds
    );
}
