package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ReasonCodes {

    OTHER("other"),
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
