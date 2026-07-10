package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
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
    DCDED("BFA1-DCDED"),

    PASTX("BFA1-PASTX"),
    RPSTX("BFA1-RPSTX"),
    EASTX("BFA1-EASTX"),
    EUSTX("BFA1-EUSTX"),
    HUSTX("BFA1-HUSTX"),
    DCSTX("BFA1-DCSTX"),

    PASTD("BFA1-PASTD"),
    RPSTD("BFA1-RPSTD"),
    EASTD("BFA1-EASTD"),
    EUSTD("BFA1-EUSTD"),
    HUSTD("BFA1-HUSTD"),
    DCSTD("BFA1-DCSTD"),

    PASTV("BFA1-PASTV"),
    EASTV("BFA1-EASTV"),
    EUSTV("BFA1-EUSTV"),
    HUSTV("BFA1-HUSTV"),

    PASTF("BFA1-PASTF"),
    RPSTF("BFA1-RPSTF"),
    EASTF("BFA1-EASTF"),
    EUSTF("BFA1-EUSTF"),
    HUSTF("BFA1-HUSTF"),
    DCSTF("BFA1-DCSTF"),

    PASTVF("BFA1-PASTVF"),
    EASTVF("BFA1-EASTVF"),
    EUSTVF("BFA1-EUSTVF"),
    HUSTVF("BFA1-HUSTVF");

    private final String value;

    CaseTypeValue(String value) {
        this.value = value;
    }

    public static CaseTypeValue from(
        AppealType appealType,
        boolean hasDeportation,
        boolean isSuitableToFloat,
        boolean isVirtualHearing,
        boolean isAppellantInDetention,
        boolean isStf24Weeks
    ) {
        return RULES.stream()
            .filter(rule -> rule.matches(
                appealType,
                hasDeportation,
                isSuitableToFloat,
                isVirtualHearing,
                isAppellantInDetention,
                isStf24Weeks
            ))
            .map(CaseTypeRule::result)
            .findFirst()
            .orElse(getDefaultCaseType(appealType, hasDeportation, isSuitableToFloat, isVirtualHearing, isAppellantInDetention, isStf24Weeks));
    }

    public static CaseTypeValue getDefaultCaseType(
        AppealType appealType,
        boolean hasDeportation,
        boolean isSuitableToFloat,
        boolean isVirtualHearing,
        boolean isAppellantInDetention,
        boolean isStf24Weeks
    ) {
        log.info("No matching case type found for appealType: {}, hasDeportation: {}, isSuitableToFloat: {}, isVirtualHearing: {}, isAppellantInDetention: {}, isStf24Weeks: {}. Using fallback logic.",
            appealType, hasDeportation, isSuitableToFloat, isVirtualHearing, isAppellantInDetention, isStf24Weeks);
        return switch (appealType) {
            case RP -> isStf24Weeks ? RPSTX : isAppellantInDetention ? RPDEX : RPX;
            case PA -> isStf24Weeks ? PASTX : isAppellantInDetention ? PADEX : PAX;
            case EA -> isStf24Weeks ? EASTX : isAppellantInDetention ? EADEX : EAX;
            case EU -> isStf24Weeks ? EUSTX : isAppellantInDetention ? EUDEX : EUX;
            case HU -> isStf24Weeks ? HUSTX : isAppellantInDetention ? HUDEX : HUX;
            case DC -> isStf24Weeks ? DCSTX : isAppellantInDetention ? DCDEX : DCX;
        };
    }

    public static final List<CaseTypeRule> RULES = List.of(
        // ---------- STANDARD ----------
        new CaseTypeRule(AppealType.RP, false, false, false, false, false, RPX),
        new CaseTypeRule(AppealType.PA, false, false, false, false, false, PAX),
        new CaseTypeRule(AppealType.EA, false, false, false, false, false, EAX),
        new CaseTypeRule(AppealType.EU, false, false, false, false, false, EUX),
        new CaseTypeRule(AppealType.HU, false, false, false, false, false, HUX),
        new CaseTypeRule(AppealType.DC, false, false, false, false, false, DCX),
        // ---------- DEPORTATION ----------
        new CaseTypeRule(AppealType.RP, true, false, false, false, false, RPD),
        new CaseTypeRule(AppealType.PA, true, false, false, false, false, PAD),
        new CaseTypeRule(AppealType.EA, true, false, false, false, false, EAD),
        new CaseTypeRule(AppealType.EU, true, false, false, false, false, EUD),
        new CaseTypeRule(AppealType.HU, true, false, false, false, false, HUD),
        new CaseTypeRule(AppealType.DC, true, false, false, false, false, DCD),
        // ---------- FLOAT ----------
        new CaseTypeRule(AppealType.RP, false, true, false, false, false, RPF),
        new CaseTypeRule(AppealType.PA, false, true, false, false, false, PAF),
        new CaseTypeRule(AppealType.EA, false, true, false, false, false, EAF),
        new CaseTypeRule(AppealType.EU, false, true, false, false, false, EUF),
        new CaseTypeRule(AppealType.HU, false, true, false, false, false, HUF),
        new CaseTypeRule(AppealType.DC, false, true, false, false, false, DCF),
        // ---------- VIRTUAL ----------
        new CaseTypeRule(AppealType.RP, false, false, true, false, false, RPX),
        new CaseTypeRule(AppealType.PA, false, false, true, false, false, PAV),
        new CaseTypeRule(AppealType.EA, false, false, true, false, false, EAV),
        new CaseTypeRule(AppealType.EU, false, false, true, false, false, EUV),
        new CaseTypeRule(AppealType.HU, false, false, true, false, false, HUV),
        new CaseTypeRule(AppealType.DC, false, false, true, false, false, DCX),
        // ---------- VIRTUAL FLOAT ----------
        new CaseTypeRule(AppealType.RP, false, true, true, false, false, RPX),
        new CaseTypeRule(AppealType.PA, false, true, true, false, false, PAVF),
        new CaseTypeRule(AppealType.EA, false, true, true, false, false, EAVF),
        new CaseTypeRule(AppealType.EU, false, true, true, false, false, EUVF),
        new CaseTypeRule(AppealType.HU, false, true, true, false, false, HUVF),
        new CaseTypeRule(AppealType.DC, false, true, true, false, false, DCX),
        // ---------- VIRTUAL DETENTION ----------
        new CaseTypeRule(AppealType.RP, false, false, true, true, false, RPX),
        new CaseTypeRule(AppealType.PA, false, false, true, true, false, PADEV),
        new CaseTypeRule(AppealType.EA, false, false, true, true, false, EADEV),
        new CaseTypeRule(AppealType.EU, false, false, true, true, false, EUDEV),
        new CaseTypeRule(AppealType.HU, false, false, true, true, false, HUDEV),
        new CaseTypeRule(AppealType.DC, false, false, true, true, false, DCX),
        // ---------- DETENTION ----------
        new CaseTypeRule(AppealType.RP, false, false, false, true, false, RPDEX),
        new CaseTypeRule(AppealType.PA, false, false, false, true, false, PADEX),
        new CaseTypeRule(AppealType.EA, false, false, false, true, false, EADEX),
        new CaseTypeRule(AppealType.EU, false, false, false, true, false, EUDEX),
        new CaseTypeRule(AppealType.HU, false, false, false, true, false, HUDEX),
        new CaseTypeRule(AppealType.DC, false, false, false, true, false, DCDEX),
        // ---------- DETENTION + DEPORTATION ----------
        new CaseTypeRule(AppealType.RP, true, false, false, true, false, RPDED),
        new CaseTypeRule(AppealType.PA, true, false, false, true, false, PADED),
        new CaseTypeRule(AppealType.EA, true, false, false, true, false, EADED),
        new CaseTypeRule(AppealType.EU, true, false, false, true, false, EUDED),
        new CaseTypeRule(AppealType.HU, true, false, false, true, false, HUDED),
        new CaseTypeRule(AppealType.DC, true, false, false, true, false, DCDED),
        // ---------- STF STANDARD ----------
        new CaseTypeRule(AppealType.RP, false, false, false, false, true, RPSTX),
        new CaseTypeRule(AppealType.PA, false, false, false, false, true, PASTX),
        new CaseTypeRule(AppealType.EA, false, false, false, false, true, EASTX),
        new CaseTypeRule(AppealType.EU, false, false, false, false, true, EUSTX),
        new CaseTypeRule(AppealType.HU, false, false, false, false, true, HUSTX),
        new CaseTypeRule(AppealType.DC, false, false, false, false, true, DCSTX),
        // ---------- STF DEPORTATION ----------
        new CaseTypeRule(AppealType.RP, true, false, false, false, true, RPSTD),
        new CaseTypeRule(AppealType.PA, true, false, false, false, true, PASTD),
        new CaseTypeRule(AppealType.EA, true, false, false, false, true, EASTD),
        new CaseTypeRule(AppealType.EU, true, false, false, false, true, EUSTD),
        new CaseTypeRule(AppealType.HU, true, false, false, false, true, HUSTD),
        new CaseTypeRule(AppealType.DC, true, false, false, false, true, DCSTD),
        // ---------- STF FLOAT ----------
        new CaseTypeRule(AppealType.RP, false, true, false, false, true, RPSTF),
        new CaseTypeRule(AppealType.PA, false, true, false, false, true, PASTF),
        new CaseTypeRule(AppealType.EA, false, true, false, false, true, EASTF),
        new CaseTypeRule(AppealType.EU, false, true, false, false, true, EUSTF),
        new CaseTypeRule(AppealType.HU, false, true, false, false, true, HUSTF),
        new CaseTypeRule(AppealType.DC, false, true, false, false, true, DCSTF),
        // ---------- STF VIRTUAL ----------
        new CaseTypeRule(AppealType.RP, false, false, true, false, true, RPSTX),
        new CaseTypeRule(AppealType.PA, false, false, true, false, true, PASTV),
        new CaseTypeRule(AppealType.EA, false, false, true, false, true, EASTV),
        new CaseTypeRule(AppealType.EU, false, false, true, false, true, EUSTV),
        new CaseTypeRule(AppealType.HU, false, false, true, false, true, HUSTV),
        new CaseTypeRule(AppealType.DC, false, false, true, false, true, DCSTX),
        // ---------- STF VIRTUAL FLOAT ----------
        new CaseTypeRule(AppealType.RP, false, true, true, false, true, RPSTX),
        new CaseTypeRule(AppealType.PA, false, true, true, false, true, PASTVF),
        new CaseTypeRule(AppealType.EA, false, true, true, false, true, EASTVF),
        new CaseTypeRule(AppealType.EU, false, true, true, false, true, EUSTVF),
        new CaseTypeRule(AppealType.HU, false, true, true, false, true, HUSTVF),
        new CaseTypeRule(AppealType.DC, false, true, true, false, true, DCSTX)
    );

    public record CaseTypeRule(
        AppealType appealType,
        boolean deportation,
        boolean suitableToFloat,
        boolean virtualHearing,
        boolean appellantInDetention,
        boolean stf24Weeks,
        CaseTypeValue result
    ) {
        boolean matches(
            AppealType appealType,
            boolean deportation,
            boolean suitableToFloat,
            boolean virtualHearing,
            boolean appellantInDetention,
            boolean stf24Weeks
        ) {
            return this.appealType == appealType
                && this.deportation == deportation
                && this.suitableToFloat == suitableToFloat
                && this.virtualHearing == virtualHearing
                && this.appellantInDetention == appellantInDetention
                && this.stf24Weeks == stf24Weeks;
        }
    }
}
