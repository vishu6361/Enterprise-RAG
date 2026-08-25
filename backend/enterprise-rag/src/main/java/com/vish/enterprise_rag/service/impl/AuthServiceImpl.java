package com.vish.enterprise_rag.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.vish.enterprise_rag.entities.User;
import com.vish.enterprise_rag.mappers.UserMapper;
import com.vish.enterprise_rag.repositories.read.UserReadRepository;
import com.vish.enterprise_rag.requests.LoginReq;
import com.vish.enterprise_rag.response.AuthRes;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.security.JwtUtils;
import com.vish.enterprise_rag.security.UserPrincipal;
import com.vish.enterprise_rag.service.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserReadRepository userReadRepository;
    private final UserMapper userMapper;

    @Override
    public ResponseEntity<?> login(LoginReq request) {
        log.info("Attempting login for user: {}", request.getEmail());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateToken(authentication);

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userReadRepository.findByIdAndIsActiveTrue(userPrincipal.getId())
                    .orElseThrow(() -> new IllegalStateException("User not found after successful authentication"));

            AuthRes authRes = AuthRes.builder()
                    .token(jwt)
                    .tokenType("Bearer")
                    .user(userMapper.toRes(user))
                    .build();

            return ResponseEntity.ok(ResponseDTO.success("Login successful", authRes));
        } catch (BadCredentialsException e) {
            log.warn("Invalid login attempt for email: {}", request.getEmail());
            return ResponseEntity.ok(ResponseDTO.error("Invalid email or password"));
        } catch (DisabledException e) {
            log.warn("Login attempt for deactivated user: {}", request.getEmail());
            return ResponseEntity.ok(ResponseDTO.error("User account is deactivated"));
        } catch (Exception e) {
            log.error("Error during authentication for user: {}", request.getEmail(), e);
            return ResponseEntity.ok(ResponseDTO.error("Authentication failed: " + e.getMessage()));
        }
    }
}
