package com.vish.enterprise_rag.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vish.enterprise_rag.service.FileStorageService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.storage.upload-dir:storage/uploads}")
    private String uploadBaseDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(uploadBaseDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
            log.info("Initialized FileStorageService at root location: {}", this.rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize file storage directory: {}", this.rootLocation, e);
            throw new RuntimeException("Could not initialize storage directory", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, Long organizationId, String contentHash) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file.");
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        // Sanitize filename
        String sanitizedFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path orgDirectory = this.rootLocation.resolve("org_" + organizationId).normalize();
        Files.createDirectories(orgDirectory);

        String savedFilename = contentHash.substring(0, 12) + "_" + sanitizedFilename;
        Path destinationFile = orgDirectory.resolve(savedFilename).normalize();

        // Security check: ensure path is within target org directory
        if (!destinationFile.startsWith(orgDirectory)) {
            throw new SecurityException("Cannot store file outside target directory namespace.");
        }

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Stored file successfully at: {}", destinationFile);
        return destinationFile.toString();
    }

    @Override
    public byte[] loadFile(String filePath) throws IOException {
        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        if (!Files.exists(path)) {
            throw new IOException("File not found at path: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            return Files.deleteIfExists(path);
        } catch (Exception e) {
            log.error("Failed to delete file at path: {}", filePath, e);
            return false;
        }
    }
}
