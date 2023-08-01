package uk.gov.hmcts.reform.iahearingsapi.domain.service.exceptions;

public class CcdDataDeserializationException extends RuntimeException {

    public CcdDataDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
