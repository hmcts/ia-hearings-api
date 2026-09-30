package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CustodyStatus {

    IN_CUSTODY("C"),
    IN_DETENTION("D");

    @JsonValue
    private final String value;

    CustodyStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
