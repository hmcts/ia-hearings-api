package uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception;

public class IdamApiException extends RuntimeException  {

    public static final String DEFAULT_MESSAGE = "IDAM api error:";

    public IdamApiException(String message) {
        super(DEFAULT_MESSAGE + message);
    }
}
