package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AppealType {

    RP("revocationOfProtection", "Revocation of a protection status"),
    PA("protection", "Refusal of protection claim"),
    EA("refusalOfEu", "Refusal of application under the EEA regulations"),
    HU("refusalOfHumanRights", "Refusal of a human rights claim"),
    DC("deprivation", "Deprivation of citizenship"),
    EU("euSettlementScheme", "EU Settlement Scheme");

    @JsonValue
    private String value;

    private String description;

    AppealType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

}
