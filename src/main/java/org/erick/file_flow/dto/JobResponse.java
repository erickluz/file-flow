package org.erick.file_flow.dto;

public record JobResponse(
    Long jobId, 
    String status, 
    Integer totalDocuments, 
    Integer documentsCreated, 
    Integer doneDocuments, 
    Integer failedDocuments) {

}
