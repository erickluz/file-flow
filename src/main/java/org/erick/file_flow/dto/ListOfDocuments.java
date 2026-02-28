package org.erick.file_flow.dto;

import java.util.List;

public record ListOfDocuments(Long jobId, List<DocumentResponse> documents) {

}
