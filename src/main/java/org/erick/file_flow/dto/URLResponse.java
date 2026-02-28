package org.erick.file_flow.dto;

public record URLResponse(
        Long jobId, 
        Long documentId, 
        String uploadURL, 
        Integer expiresInSeconds
    ) {
}
