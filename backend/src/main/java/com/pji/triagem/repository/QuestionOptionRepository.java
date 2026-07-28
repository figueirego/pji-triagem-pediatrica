package com.pji.triagem.repository;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.model.QuestionOption;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionOptionRepository extends BaseRepository<QuestionOption, Long> {

    List<QuestionOption> findByQuestionIdOrderByOrderAsc(Long questionId);

    @Query("""
            SELECT qo
            FROM QuestionOption qo
            WHERE qo.question.id IN :questionIds
            ORDER BY qo.question.id ASC, qo.order ASC, qo.id ASC
            """)
    List<QuestionOption> findByQuestionIdInOrderByOrderAsc(@Param("questionIds") List<Long> questionIds);

    Optional<QuestionOption> findByIdAndQuestionId(Long id, Long questionId);
}
