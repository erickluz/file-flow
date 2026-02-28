package org.erick.file_flow.domain;

public enum JobStatus {
    CREATED (1, "Job created"),
    COLLECTING (2, "Job collecting documents"),
    READ_FOR_UPLOAD (3, "Job ready for upload"),
    PROCESSING (4, "Job processing"),
    DONE (5, "Job done"),
    FAILED (6, "Job failed");

    private Integer codigo;
    private String description;

    private JobStatus(Integer codigo, String description) {
        this.codigo = codigo;
        this.description = description;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getDescription() {
        return description;
    }


}
