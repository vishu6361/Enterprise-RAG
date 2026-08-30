package com.vish.enterprise_rag.controllers;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vish.enterprise_rag.requests.LoginReq;
import com.vish.enterprise_rag.requests.SignupReq;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginReq request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.ok(
                    ResponseDTO.error(
                            result.getAllErrors()
                                    .stream()
                                    .map(e -> e.getDefaultMessage())
                                    .collect(Collectors.toList())
                                    .toString()
                    )
            );
        }
        return authService.login(request);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupReq request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.ok(
                    ResponseDTO.error(
                            result.getAllErrors()
                                    .stream()
                                    .map(e -> e.getDefaultMessage())
                                    .collect(Collectors.toList())
                                    .toString()
                    )
            );
        }
        return authService.signup(request);
    }
}
