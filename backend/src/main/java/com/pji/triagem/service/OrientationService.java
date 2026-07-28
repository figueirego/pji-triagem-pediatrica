package com.pji.triagem.service;

import com.pji.triagem.base.service.BaseService;
import com.pji.triagem.dto.response.OrientationResponse;
import com.pji.triagem.model.Orientation;

public interface OrientationService extends BaseService<Orientation> {

    OrientationResponse getOrientationByAssessmentId(Long assessmentId);
}
