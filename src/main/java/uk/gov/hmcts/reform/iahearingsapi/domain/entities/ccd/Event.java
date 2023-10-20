package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    LIST_CASE("listCase"),
    UPDATE_HEARING_REQUEST("updateHearingRequest"),
    HANDLE_HEARING_EXCEPTION("handleHearingException"),
    UPDATE_HMC_RESPONSE("updateHMCResponse"),
    RECORD_ADJOURNMENT_DETAILS("recordAdjournmentDetails"),
    END_APPEAL("endAppeal"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
