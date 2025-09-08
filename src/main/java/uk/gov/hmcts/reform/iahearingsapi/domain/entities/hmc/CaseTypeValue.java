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
    PAF("BFA1-PAF"),
    RPF("BFA1-RPF"),
    EAF("BFA1-EAF"),
    EUF("BFA1-EUF"),
    HUF("BFA1-HUF"),
    DCF("BFA1-DCF"),
    PAV("BFA1-PAV"),
    EAV("BFA1-EAV"),
    EUV("BFA1-EUV"),
    HUV("BFA1-HUV"),
    PAVF("BFA1-PAVF"),
    EAVF("BFA1-EAVF"),
    EUVF("BFA1-EUVF"),
    HUVF("BFA1-HUVF"),
    PADEV("BFA1-PADEV"),
    EADEV("BFA1-EADEV"),
    EUDEV("BFA1-EUDEV"),
    HUDEV("BFA1-HUDEV"),
    PADEX("BFA1-PADEX"),
    RPDEX("BFA1-RPDEX"),
    EADEX("BFA1-EADEX"),
    EUDEX("BFA1-EUDEX"),
    HUDEX("BFA1-HUDEX"),
    DCDEX("BFA1-DCDEX"),
    PADED("BFA1-PADED"),
    RPDED("BFA1-RPDED"),
    EADED("BFA1-EADED"),
    EUDED("BFA1-EUDED"),
    HUDED("BFA1-HUDED"),
    DCDED("BFA1-DCDED")
    ;

    private final String value;

    CaseTypeValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
