package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ReasonCodes {

    OTHER("no-interpreter-available"),
    ;

    @JsonValue
    private final String value;


    ReasonCodes(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
