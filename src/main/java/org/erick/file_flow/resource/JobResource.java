package org.erick.file_flow.resource;

import org.apache.catalina.connector.Response;
import org.erick.file_flow.JobService;
import org.erick.file_flow.domain.Job;
import org.erick.file_flow.domain.JobDocument;
import org.erick.file_flow.dto.DocumentRequest;
import org.erick.file_flow.dto.DocumentResponse;
import org.erick.file_flow.dto.JobRequest;
import org.erick.file_flow.dto.JobResponse;
import org.erick.file_flow.dto.ListOfDocuments;
import org.erick.file_flow.dto.URLResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobResource { 

    @Autowired
    private JobService jobService;

    @Value("${app.url.expiration-seconds:3600}")
    private Integer URLexpiresInSeconds;
    
    @PostMapping("/create")
    public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest request) {
        Job job = jobService.createJob(request.totalDocuments());
        JobResponse response = new JobResponse(
            job.getId(), 
            job.getStatus().getDescription(),  
            job.getDocuments().size(),
            job.getDocumentsCreated(),
            job.getDoneDocuments(),
            job.getFailedDocuments()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{jobId}/documents/create")
    public ResponseEntity<DocumentResponse> createDocument(@PathVariable Long jobId,
            @RequestBody DocumentRequest documentRequest) {
        JobDocument document = jobService.createDocumentIntoJob(
            documentRequest.originalFilename(),
            documentRequest.contentType(),
            jobId
        );
        DocumentResponse response = new DocumentResponse(
            jobId,
            document.getId(),
            document.getStatus().name(),
            null,
            null,
            document.getRawKey(),
            document.getResultKey(),
            null,
            document.getSizeBytes(),
            document.geteTag()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{jobId}/documents/{documentId}/upload-url")
    public ResponseEntity<URLResponse> getGeneratedURL(@PathVariable Long jobId, @PathVariable Long documentId) {
        String url = jobService.getGeneratedURL(jobId, documentId);
        URLResponse response = new URLResponse(jobId, documentId, url, URLexpiresInSeconds);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long jobId) {
        Job job = jobService.findById(jobId);
        return ResponseEntity.ok(
            new JobResponse(
                job.getId(),
                job.getStatus().getDescription(), 
                job.getDocuments().size(), 
                job.getDocumentsCreated(), 
                job.getDoneDocuments(), 
                job.getFailedDocuments()
            )
        );
    }

    @GetMapping("/{jobId}/documents")
    public ResponseEntity<ListOfDocuments> getDocumentsByJobId(@PathVariable Long jobId) {
        Job job = jobService.findById(jobId);
        ListOfDocuments response = new ListOfDocuments(jobId, job.getDocuments().stream()
        .map(document -> new DocumentResponse(
            jobId,
            document.getId(),
            document.getStatus().name(),
            document.getOriginalFilename(),
            document.getContentType(),
            document.getRawKey(),
            document.getResultKey(),
            document.getErrorMessage(),
            document.getSizeBytes(),
            document.geteTag()
        )).toList()            
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}/documents/{documentId}")
    public ResponseEntity<DocumentResponse> getDocumentById(@PathVariable Long jobId, @PathVariable Long documentId) {
        Job job = jobService.findById(jobId);
        JobDocument document = job.getDocuments().stream()
            .filter(doc -> doc.getId().equals(documentId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Document not found with id: " + documentId));
        
        DocumentResponse response = new DocumentResponse(
            jobId,
            document.getId(),
            document.getStatus().name(),
            document.getOriginalFilename(),
            document.getContentType(),
            document.getRawKey(),
            document.getResultKey(),
            document.getErrorMessage(),
            document.getSizeBytes(),
            document.geteTag()
        );
        return ResponseEntity.ok(response);
    }

}
