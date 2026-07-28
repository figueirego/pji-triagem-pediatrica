package com.pji.triagem.service;

import com.pji.triagem.base.service.BaseService;
import com.pji.triagem.model.YesNoWeight;

public interface YesNoWeightService extends BaseService<YesNoWeight> {

    YesNoWeight findByQuestion(Long questionId);
}
