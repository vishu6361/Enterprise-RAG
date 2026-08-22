package com.vish.enterprise_rag.repositories.read;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.User;

public interface UserReadRepository extends JpaRepository<User, Long> {
    Optional<User> findByIdAndIsActiveTrue(Long id);
    Optional<User> findByEmailAndIsActiveTrue(String email);
    List<User> findByIsActiveTrue();
}
