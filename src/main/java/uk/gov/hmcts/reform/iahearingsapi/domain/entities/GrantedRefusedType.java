package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

public enum GrantedRefusedType {

    GRANTED("Granted"),
    REFUSED("Refused");

    private final String value;

    GrantedRefusedType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
