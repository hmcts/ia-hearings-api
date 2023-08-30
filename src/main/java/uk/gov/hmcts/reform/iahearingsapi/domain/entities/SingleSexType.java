package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

public enum SingleSexType {

    MALE("All male"),
    FEMALE("All female");

    private final String value;

    SingleSexType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
