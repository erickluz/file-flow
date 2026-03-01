package org.erick.file_flow.service;

import java.time.LocalDateTime;

import org.erick.file_flow.domain.DocumentStatus;
import org.erick.file_flow.domain.Job;
import org.erick.file_flow.domain.JobDocument;
import org.erick.file_flow.domain.JobStatus;
import org.erick.file_flow.exception.JobException;
import org.erick.file_flow.repository.JobDocumentRepository;
import org.erick.file_flow.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobDocumentRepository jobDocumentRepository;

    public Job createJob(Integer totalDocuments) {
        Job job = new Job(JobStatus.CREATED, 
            totalDocuments, 
            0, 
            0, 
            0, 
            LocalDateTime.now(), 
            LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Transactional
    public JobDocument createDocumentIntoJob(String originalFilename, String contentType, Long jobId) {
        JobDocument document = new JobDocument();
        document.setOriginalFilename(originalFilename);
        document.setContentType(contentType);
        document.setStatus(DocumentStatus.READY_FOR_UPLOAD);
        document.setCreatedAt(LocalDateTime.now());

        jobRepository.findById(jobId).ifPresentOrElse(job -> {
            job.getDocuments().add(document);
            job.setUpdatedAt(LocalDateTime.now());
            job.setDocumentsCreated(job.getDocumentsCreated()+1);

            job = jobRepository.save(job);
            document.setJob(job);
            jobDocumentRepository.save(document);
            
        }, () -> {
            throw new JobException("Job not found with id: " + jobId);
        });
        return document;
    }

    public String getGeneratedURL(Long jobId, Long documentId) {
        // AWS S3 SDK code to generate a pre-signed URL for the document upload
        return "https://example.com/upload-url";
    }

    public Job findById(Long jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> 
            new JobException("Job not found with id: " + jobId)
        );
    }
    
    
}
