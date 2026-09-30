package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UnavailabilityType {

    AM("AM"),
    PM("PM"),
    ALL_DAY("All Day");

    @JsonValue
    private final String unavailabilityType;

    UnavailabilityType(String unavailabilityType) {
        this.unavailabilityType = unavailabilityType;
    }

    public String getUnavailabilityType() {
        return unavailabilityType;
    }
}
