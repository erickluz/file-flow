package org.erick.file_flow.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Job {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private JobStatus status;
    private Integer totalDocuments;
    private Integer documentsCreated;
    private Integer doneDocuments;
    private Integer failedDocuments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "job", fetch = FetchType.EAGER)
    private List<JobDocument> documents = new ArrayList<>();

    public Job() {
    }

    public Job(JobStatus status, Integer totalDocuments, Integer documentsCreated, Integer doneDocuments,
            Integer failedDocuments, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.status = status;
        this.totalDocuments = totalDocuments;
        this.documentsCreated = documentsCreated;
        this.doneDocuments = doneDocuments;
        this.failedDocuments = failedDocuments;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public JobStatus getStatus() {
        return status;
    }
    public void setStatus(JobStatus status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public List<JobDocument> getDocuments() {
        return documents;
    }
    public Integer getTotalDocuments() {
        return totalDocuments;
    }
    public void setTotalDocuments(Integer totalDocuments) {
        this.totalDocuments = totalDocuments;
    }
    public Integer getDocumentsCreated() {
        return documentsCreated;
    }
    public void setDocumentsCreated(Integer documentsCreated) {
        this.documentsCreated = documentsCreated;
    }
    public Integer getDoneDocuments() {
        return doneDocuments;
    }
    public void setDoneDocuments(Integer doneDocuments) {
        this.doneDocuments = doneDocuments;
    }
    public Integer getFailedDocuments() {
        return failedDocuments;
    }
    public void setFailedDocuments(Integer failedDocuments) {
        this.failedDocuments = failedDocuments;
    }
    public void setDocuments(List<JobDocument> documents) {
        this.documents = documents;
    }

}
