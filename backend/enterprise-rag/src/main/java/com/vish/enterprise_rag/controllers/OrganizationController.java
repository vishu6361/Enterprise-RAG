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

import com.vish.enterprise_rag.requests.OrganizationReq;
import com.vish.enterprise_rag.response.ResponseDTO;
import com.vish.enterprise_rag.service.OrganizationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<?> createOrganization(@Valid @RequestBody OrganizationReq request, BindingResult result) {
        log.info("Creating organization: {}", request);
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
        return organizationService.createOrganization(request);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateOrganization(@PathVariable long id, @RequestBody OrganizationReq request) {
        log.info("Updating organization: {} with ID: {}", request, id);
        return organizationService.updateOrganization(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrganization(@PathVariable long id) {
        log.info("Deleting organization with ID: {}", id);
        return organizationService.deleteOrganization(id);
    }

    @GetMapping
    public ResponseEntity<?> getAllOrganizations() {
        log.info("Getting all organizations");
        return organizationService.getAllOrganizations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrganization(@PathVariable long id) {
        log.info("Getting organization with ID: {}", id);
        return organizationService.getOrganization(id);
    }

}
