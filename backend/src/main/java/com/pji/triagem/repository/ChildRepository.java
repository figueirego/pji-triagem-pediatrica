package com.pji.triagem.repository;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.model.Child;

import java.util.List;
import java.util.Optional;

public interface ChildRepository extends BaseRepository<Child, Long> {

    List<Child> findByUserId(Long userId);

    Optional<Child> findByIdAndUserId(Long id, Long userId);
}
