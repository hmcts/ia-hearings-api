package uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception;

import static org.apache.commons.lang3.StringUtils.defaultString;

public class HmcException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "Failed call to HMC Hearing service. Error: ";

    public HmcException(Throwable t) {
        super(MESSAGE_TEMPLATE + defaultString(t.getMessage()), t);
    }

    public HmcException(String message) {
        super(MESSAGE_TEMPLATE + message);
    }
}
