package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

public enum CaseTypeValue {
    RPX("BFA1-RPX"),
    PAX("BFA1-PAX"),
    EAX("BFA1-EAX"),
    EUX("BFA1-EUX"),
    HUX("BFA1-HUX"),
    DCX("BFA1-DCX"),
    RPD("BFA1-RPD"),
    PAD("BFA1-PAD"),
    EAD("BFA1-EAD"),
    EUD("BFA1-EUD"),
    HUD("BFA1-HUD"),
    DCD("BFA1-DCD"),
    ;

    private final String value;

    CaseTypeValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
