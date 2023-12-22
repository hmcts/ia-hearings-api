package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    LIST_CASE("listCase"),
    UPDATE_HEARING_REQUEST("updateHearingRequest"),
    HANDLE_HEARING_EXCEPTION("handleHearingException"),
    TRIGGER_CMR_UPDATED("triggerCmrUpdated"),
    RECORD_ADJOURNMENT_DETAILS("recordAdjournmentDetails"),
    END_APPEAL("endAppeal"),
    EDIT_CASE_LISTING("editCaseListing"),
    START_APPEAL("startAppeal"),
    EDIT_APPEAL("editAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    LIST_CASE_FOR_FT_ONLY("listCaseForFTOnly"),
    UPDATE_INTERPRETER_DETAILS("updateInterpreterDetails"),
    UPDATE_INTERPRETER_BOOKING_STATUS("updateInterpreterBookingStatus"),
    TRIGGER_CMR_LISTED("triggerCmrListed"),
    TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK("triggerReviewInterpreterBookingTask"),

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
