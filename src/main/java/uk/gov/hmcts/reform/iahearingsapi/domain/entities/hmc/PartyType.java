package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PartyType {

    IND("IND"),
    ORG("ORG");

    @JsonValue
    private final String partyType;

    PartyType(String partyType) {
        this.partyType = partyType;
    }

    public String getPartyType() {
        return partyType;
    }
}
