package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    LIST_CASE("listCase"),
    UPDATE_HEARING_REQUEST("updateHearingRequest"),
    END_APPEAL("endAppeal"),
    START_APPEAL("startAppeal"),
    EDIT_APPEAL("editAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    LIST_CASE_FOR_FT_ONLY("listCaseForFTOnly"),

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
