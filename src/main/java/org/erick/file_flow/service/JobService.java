package org.erick.file_flow.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.erick.file_flow.domain.DocumentStatus;
import org.erick.file_flow.domain.Job;
import org.erick.file_flow.domain.JobDocument;
import org.erick.file_flow.domain.JobStatus;
import org.erick.file_flow.exception.JobException;
import org.erick.file_flow.repository.JobDocumentRepository;
import org.erick.file_flow.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class JobService {

    @Value("${URL_TIME_DURATION_MINUTES:10}")
    private Long URLTimeDurationMinutes;
    @Value("${AWS_S3_BUCKET_NAME:erick-luz-files-flow}")
    private String bucketName;

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private JobDocumentRepository jobDocumentRepository;
    @Autowired
    private AWSService AWSService;

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
        document.setDocumentUUID(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
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

            document.setRawKey("raw/" + jobId + "/" + document.getDocumentUUID() + "/" + originalFilename);
            document.setResultKey("processed/" + jobId + "/" + document.getDocumentUUID() + "/result.json");
            document.setJob(job);

            jobDocumentRepository.save(document);
            
        }, () -> {
            throw new JobException("Job not found with id: " + jobId, HttpStatus.BAD_REQUEST);
        });
        return document;
    }

    public String getGeneratedURL(Long jobId, Long documentId) {
        JobDocument document = jobDocumentRepository.findByJobIdAndDocumentUUID(jobId, documentId).orElseThrow(() -> 
            new JobException("Document not found with id: " + documentId, HttpStatus.NOT_FOUND)
        );
        return AWSService.createPresignedUrl(bucketName, document.getRawKey(), document.getContentType(), URLTimeDurationMinutes);
    }

    public Job findById(@NonNull Long jobId) {
        return jobRepository.findById(jobId).orElseThrow(() -> 
            new JobException("Job not found with id: " + jobId, HttpStatus.NOT_FOUND)
        );
    }
    
    
}