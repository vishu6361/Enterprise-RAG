package com.vish.enterprise_rag.repositories.write;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vish.enterprise_rag.entities.User;

public interface UserWriteRepository extends JpaRepository<User, Long> {
    
}
