package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AsylumCaseHearingOutcome {
    GRANTED("granted"),
    DISMISSED("dismissed");

    @JsonValue
    private final String id;

    AsylumCaseHearingOutcome(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
