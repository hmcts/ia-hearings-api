package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AsylumCaseDecision {
    GRANTED("granted"),
    DISMISSED("dismissed"),

    @JsonEnumDefaultValue
    NONE("none");

    @JsonValue
    private final String id;

    AsylumCaseDecision(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
