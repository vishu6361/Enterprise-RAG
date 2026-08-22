package com.vish.enterprise_rag.controllers;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vish.enterprise_rag.requests.UserReq;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserReq request, BindingResult result) {
        log.info("Creating user: {}", request);
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
        return userService.createUser(request);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable long id, @RequestBody UserReq request) {
        log.info("Updating user: {} with ID: {}", request, id);
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id) {
        log.info("Deleting user with ID: {}", id);
        return userService.deleteUser(id);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        log.info("Getting all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id) {
        log.info("Getting user with ID: {}", id);
        return userService.getUser(id);
    }

}
