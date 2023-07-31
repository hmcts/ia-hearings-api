package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RequirementType {

    MUSTINC("MUSTINC"),
    OPTINC("OPTINC"),
    EXCLUDE("EXCLUDE");

    @JsonValue
    private final String requirementType;

    RequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    public String getRequirementType() {
        return requirementType;
    }
}
