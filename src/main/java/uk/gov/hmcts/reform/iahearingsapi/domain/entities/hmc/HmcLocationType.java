package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HmcLocationType {

    COURT("court"),
    CLUSTER("cluster"),
    REGION("regionId");

    @JsonValue
    private final String locationType;

    @JsonCreator
    HmcLocationType(String locationType) {
        this.locationType = locationType;
    }

    public String getLocationType() {
        return locationType;
    }
}
