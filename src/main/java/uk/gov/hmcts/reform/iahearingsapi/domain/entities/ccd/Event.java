package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    LIST_CASE("listCase"),
    UPDATE_HEARING_REQUEST("updateHearingRequest"),
    HANDLE_HEARING_EXCEPTION("handleHearingException"),
    CMR_RE_LISTING("cmrReListing"),
    RECORD_ADJOURNMENT_DETAILS("recordAdjournmentDetails"),
    END_APPEAL("endAppeal"),
    EDIT_CASE_LISTING("editCaseListing"),
    START_APPEAL("startAppeal"),
    EDIT_APPEAL("editAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    LIST_CASE_FOR_FT_ONLY("listCaseForFTOnly"),
    UPDATE_INTERPRETER_DETAILS("updateInterpreterDetails"),
    UPDATE_INTERPRETER_BOOKING_STATUS("updateInterpreterBookingStatus"),
    CMR_LISTING("cmrListing"),
    DECIDE_AN_APPLICATION("decideAnApplication"),
    CASE_LISTING("caseListing"),
    LIST_CASE_WITHOUT_HEARING_REQUIREMENTS("listCaseWithoutHearingRequirements"),
    DECISION_WITHOUT_HEARING_LISTED("decisionWithoutHearingListed"),
    REVIEW_HEARING_REQUIREMENTS("reviewHearingRequirements"),
    DECISION_AND_REASONS_STARTED("decisionAndReasonsStarted"),
    RESTORE_STATE_FROM_ADJOURN("restoreStateFromAdjourn"),
    UPDATE_NEXT_HEARING_INFO("updateNextHearingInfo"),
    HEARING_CANCELLED("hearingCancelled"),

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
