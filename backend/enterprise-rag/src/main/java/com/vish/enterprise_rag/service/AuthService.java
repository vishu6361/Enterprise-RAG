package com.vish.enterprise_rag.service;

import org.springframework.http.ResponseEntity;

import com.vish.enterprise_rag.requests.LoginReq;
import com.vish.enterprise_rag.requests.SignupReq;

public interface AuthService {
    ResponseEntity<?> login(LoginReq request);
    ResponseEntity<?> signup(SignupReq request);
}
