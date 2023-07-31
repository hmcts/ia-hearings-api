package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PriorityType {

    STANDARD("Standard"),
    URGENT("Urgent");

    @JsonValue
    private final String priorityType;

    PriorityType(String priorityType) {
        this.priorityType = priorityType;
    }

    public String getPriorityType() {
        return priorityType;
    }
}
