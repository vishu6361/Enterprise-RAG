package com.vish.enterprise_rag.service;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    
    /**
     * Stores an uploaded multipart file to disk under organization namespace.
     * 
     * @param file the multipart file
     * @param organizationId tenant organization ID
     * @param contentHash SHA-256 hash of the file
     * @return absolute path string of the saved file
     * @throws IOException on I/O error
     */
    String storeFile(MultipartFile file, Long organizationId, String contentHash) throws IOException;

    /**
     * Loads raw file bytes from disk.
     * 
     * @param filePath absolute path to file
     * @return file byte array
     * @throws IOException on I/O error
     */
    byte[] loadFile(String filePath) throws IOException;

    /**
     * Deletes a file from disk.
     * 
     * @param filePath absolute path to file
     * @return true if deleted
     */
    boolean deleteFile(String filePath);
}
