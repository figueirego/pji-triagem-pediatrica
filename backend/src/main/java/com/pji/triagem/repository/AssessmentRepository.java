package com.pji.triagem.repository;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.model.Assessment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssessmentRepository extends BaseRepository<Assessment, Long> {

    List<Assessment> findByChildId(Long childId, Sort sort);

    List<Assessment> findByChildIdAndChildUserId(Long childId, Long userId, Sort sort);

    @Query("""
            SELECT a
            FROM Assessment a
            JOIN FETCH a.child c
            WHERE c.user.id = :userId
            ORDER BY a.createdAt DESC
            """)
    List<Assessment> findByChildUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("""
            SELECT a
            FROM Assessment a
            JOIN FETCH a.child c
            WHERE c.id = :childId
              AND c.user.id = :userId
            ORDER BY a.createdAt DESC
            """)
    List<Assessment> findByChildIdAndChildUserIdOrderByCreatedAtDesc(
            @Param("childId") Long childId,
            @Param("userId") Long userId
    );

    Long countByChildId(Long childId);

    @Query("SELECT MAX(a.createdAt) FROM Assessment a WHERE a.child.id = :childId")
    Optional<LocalDateTime> findLastAssessmentDateByChildId(@Param("childId") Long childId);
}
