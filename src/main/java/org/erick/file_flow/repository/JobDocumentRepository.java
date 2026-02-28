package org.erick.file_flow.repository;

import org.erick.file_flow.domain.JobDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobDocumentRepository extends JpaRepository<JobDocument, Long> {

}
