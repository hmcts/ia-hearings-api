package uk.gov.hmcts.reform.iahearingsapi.domain;

public class RequiredFieldMissingException extends RuntimeException {
    public RequiredFieldMissingException(String message) {
        super(message);
    }
}
