package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

public enum HmcStatus {
    HEARING_REQUESTED,
    AWAITING_LISTING,
    UPDATE_REQUESTED,
    UPDATE_SUBMITTED,
    LISTED,
    CANCELLATION_REQUESTED,
    CANCELLATION_SUBMITTED,
    CANCELLED,
    CLOSED,
    EXCEPTION,
    VACATED,
    AWAITING_ACTUALS,
    COMPLETED,
    ADJOURNED
}
