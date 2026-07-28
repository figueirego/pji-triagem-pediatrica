package com.pji.triagem.repository;

import com.pji.triagem.base.repository.BaseRepository;
import com.pji.triagem.model.TypeUser;
import com.pji.triagem.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {


    @Query("SELECT u FROM User u WHERE (u.cpf = :id OR LOWER(u.email) = LOWER(:id)) AND u.type = :type")
    Optional<User> findByLoginAndType(String id, TypeUser type);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.cpf = :id AND u.type = :type")
    boolean existsByLoginAndType(@Param("id") String id, @Param("type") TypeUser type);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.type = :type")
    boolean existsByEmailAndType(@Param("email") String email, @Param("type") TypeUser type);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE User u SET u.attemptsCount = :attemptsCount WHERE (u.cpf = :login OR LOWER(u.email) = LOWER(:login)) AND u.type = :type")
    void updateAttemptsByLoginAndType(String login, TypeUser type, int attemptsCount);

    @Query("SELECT u FROM User u WHERE u.id = :id and u.type = :type")
    Optional<User> findByIdType(Long id, TypeUser type);


    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM User u WHERE u.cpf = :login")
    boolean existLogin(@Param("login") String login);

}
