package uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception;

import java.io.Serial;

public class HmcEventProcessingException extends Exception {

    @Serial private static final long serialVersionUID = -7206765397565397123L;

    public HmcEventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}