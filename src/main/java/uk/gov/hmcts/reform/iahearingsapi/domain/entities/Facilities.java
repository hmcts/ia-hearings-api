package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Facilities {

    IAC_TYPE_C_CONFERENCE_EQUIPMENT("33"),
    ;

    @JsonValue
    private final String value;


    Facilities(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
