package org.erick.file_flow.exception;
import org.springframework.http.HttpStatus;

public class JobException extends RuntimeException {

    private final HttpStatus httpStatus;

    public JobException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public JobException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

}
