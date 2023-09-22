package uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception;

public class HmcException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Failed to retrieve data from HMC";

    public HmcException(Throwable t) {
        super(MESSAGE_TEMPLATE, t);
    }
}