package org.erick.file_flow.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.erick.file_flow.domain.DocumentStatus;
import org.erick.file_flow.domain.Job;
import org.erick.file_flow.domain.JobDocument;
import org.erick.file_flow.domain.JobStatus;
import org.erick.file_flow.exception.JobException;
import org.erick.file_flow.repository.JobDocumentRepository;
import org.erick.file_flow.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobDocumentRepository jobDocumentRepository;

    public Job createJob(Integer totalDocuments) {
        if (totalDocuments == null || totalDocuments <= 0) {
            throw new JobException("Total documents must be greater than zero");
        }
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
    public JobDocument createDocumentIntoJob(String originalFilename, String contentType, @NonNull Long jobId) {
        JobDocument document = new JobDocument();
        document.setDocumentID(UUID.randomUUID().getMostSignificantBits());
        document.setOriginalFilename(originalFilename);
        document.setContentType(contentType);
        document.setStatus(DocumentStatus.READY_FOR_UPLOAD);
        document.setCreatedAt(LocalDateTime.now());

        jobRepository.findById(jobId).ifPresentOrElse(job -> {
            Integer newCountDocuments = job.getDocumentsCreated()+1;
            if (newCountDocuments > job.getTotalDocuments()) {
                throw new JobException("Cannot add more documents than the total specified for the job", HttpStatus.CONFLICT);
            }
            job.getDocuments().add(document);
            job.setUpdatedAt(LocalDateTime.now());
            job.setDocumentsCreated(newCountDocuments);
            if (newCountDocuments.equals(1)) {
                job.setStatus(JobStatus.COLLECTING);
            } else {
                job.setStatus((newCountDocuments.equals(job.getTotalDocuments())) ? JobStatus.READ_FOR_UPLOAD : JobStatus.COLLECTING);
            }
            job = jobRepository.save(job);

            document.setRawKey("raw/" + jobId + "/" + document.getDocumentID() + "/" + originalFilename);
            document.setResultKey("processed/" + jobId + "/" + document.getDocumentID() + "/result.json");
            document.setJob(job);

            jobDocumentRepository.save(document);
            
        }, () -> {
            throw new JobException("Job not found with id: " + jobId, HttpStatus.BAD_REQUEST);
        });
        return document;
    }

    public String getGeneratedURL(Long jobId, Long documentId) {
        // AWS S3 SDK code to generate a pre-signed URL for the document upload
        return "https://example.com/upload-url";
    }

    public Job findById(Long jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> 
            new JobException("Job not found with id: " + jobId, HttpStatus.NOT_FOUND)
        );
    }
    
    
}