package com.vish.enterprise_rag.service;

import org.springframework.http.ResponseEntity;

import com.vish.enterprise_rag.requests.UserReq;

public interface UserService {

    ResponseEntity<?> createUser(UserReq request);

    ResponseEntity<?> updateUser(long id, UserReq request);

    ResponseEntity<?> deleteUser(long id);

    ResponseEntity<?> getAllUsers();

    ResponseEntity<?> getUser(long id);

}
