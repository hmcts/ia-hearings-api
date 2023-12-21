package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PriorityType {

    STANDARD("Standard"),
    URGENT("Urgent");

    @JsonValue
    private final String value;

    PriorityType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
