package org.erick.file_flow.dto;

public record DocumentResponse (
        Long jobId,
        Long documentId,
        Long documentUUID,
        String status,
        String originalFilename,
        String contentType,
        String rawKey,
        String resultKey,
        String errorMessage,
        Long sizeBytes,
        String eTag
    ) {

}
