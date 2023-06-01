package uk.gov.hmcts.reform.iahearingsapi.domain.model.hmc.reference;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HmcStatus {
    HEARING_REQUESTED("Hearing requested"),
    AWAITING_LISTING("Awaiting listing"),
    LISTED("Listed"),
    UPDATE_REQUESTED("Update requested"),
    UPDATE_SUBMITTED("Update submitted"),
    EXCEPTION("Exception"),
    CANCELLATION_REQUESTED("Cancellation requested"),
    CANCELLATION_SUBMITTED("Cancellation submitted"),
    CANCELLED("Cancelled"),
    AWAITING_ACTUALS("Awaiting Actuals"),
    COMPLETED("Completed"),
    ADJOURNED("Adjourned");

    private final String label;
}
