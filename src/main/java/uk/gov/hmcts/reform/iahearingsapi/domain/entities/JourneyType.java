package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

public enum JourneyType {

    AIP("aip"),
    REP("rep");

    private final String value;

    JourneyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
