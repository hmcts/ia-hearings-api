package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CaseTypeValue {

    RPX("BFA1-RPX", AppealType.RP, false, false, false, false, false),
    PAX("BFA1-PAX", AppealType.PA, false, false, false, false, false),
    EAX("BFA1-EAX", AppealType.EA, false, false, false, false, false),
    EUX("BFA1-EUX", AppealType.EU, false, false, false, false, false),
    HUX("BFA1-HUX", AppealType.HU, false, false, false, false, false),
    DCX("BFA1-DCX", AppealType.DC, false, false, false, false, false),

    RPD("BFA1-RPD", AppealType.RP, true, false, false, false, false),
    PAD("BFA1-PAD", AppealType.PA, true, false, false, false, false),
    EAD("BFA1-EAD", AppealType.EA, true, false, false, false, false),
    EUD("BFA1-EUD", AppealType.EU, true, false, false, false, false),
    HUD("BFA1-HUD", AppealType.HU, true, false, false, false, false),
    DCD("BFA1-DCD", AppealType.DC, true, false, false, false, false),

    PAF("BFA1-PAF", AppealType.PA, false, true, false, false, false),
    RPF("BFA1-RPF", AppealType.RP, false, true, false, false, false),
    EAF("BFA1-EAF", AppealType.EA, false, true, false, false, false),
    EUF("BFA1-EUF", AppealType.EU, false, true, false, false, false),
    HUF("BFA1-HUF", AppealType.HU, false, true, false, false, false),
    DCF("BFA1-DCF", AppealType.DC, false, true, false, false, false),

    PAV("BFA1-PAV", AppealType.PA, false, false, true, false, false),
    EAV("BFA1-EAV", AppealType.EA, false, false, true, false, false),
    EUV("BFA1-EUV", AppealType.EU, false, false, true, false, false),
    HUV("BFA1-HUV", AppealType.HU, false, false, true, false, false),

    PAVF("BFA1-PAVF", AppealType.PA, false, true, true, false, false),
    EAVF("BFA1-EAVF", AppealType.EA, false, true, true, false, false),
    EUVF("BFA1-EUVF", AppealType.EU, false, true, true, false, false),
    HUVF("BFA1-HUVF", AppealType.HU, false, true, true, false, false),

    PADEV("BFA1-PADEV", AppealType.PA, false, false, true, true, false),
    EADEV("BFA1-EADEV", AppealType.EA, false, false, true, true, false),
    EUDEV("BFA1-EUDEV", AppealType.EU, false, false, true, true, false),
    HUDEV("BFA1-HUDEV", AppealType.HU, false, false, true, true, false),

    PADEX("BFA1-PADEX", AppealType.PA, false, false, false, true, false),
    RPDEX("BFA1-RPDEX", AppealType.RP, false, false, false, true, false),
    EADEX("BFA1-EADEX", AppealType.EA, false, false, false, true, false),
    EUDEX("BFA1-EUDEX", AppealType.EU, false, false, false, true, false),
    HUDEX("BFA1-HUDEX", AppealType.HU, false, false, false, true, false),
    DCDEX("BFA1-DCDEX", AppealType.DC, false, false, false, true, false),

    PADED("BFA1-PADED", AppealType.PA, true, false, false, true, false),
    RPDED("BFA1-RPDED", AppealType.RP, true, false, false, true, false),
    EADED("BFA1-EADED", AppealType.EA, true, false, false, true, false),
    EUDED("BFA1-EUDED", AppealType.EU, true, false, false, true, false),
    HUDED("BFA1-HUDED", AppealType.HU, true, false, false, true, false),
    DCDED("BFA1-DCDED", AppealType.DC, true, false, false, true, false),

    PASTX("BFA1-PASTX", AppealType.PA, false, false, false, false, true),
    RPSTX("BFA1-RPSTX", AppealType.RP, false, false, false, false, true),
    EASTX("BFA1-EASTX", AppealType.EA, false, false, false, false, true),
    EUSTX("BFA1-EUSTX", AppealType.EU, false, false, false, false, true),
    HUSTX("BFA1-HUSTX", AppealType.HU, false, false, false, false, true),
    DCSTX("BFA1-DCSTX", AppealType.DC, false, false, false, false, true),

    PASTD("BFA1-PASTD", AppealType.PA, true, false, false, false, true),
    RPSTD("BFA1-RPSTD", AppealType.RP, true, false, false, false, true),
    EASTD("BFA1-EASTD", AppealType.EA, true, false, false, false, true),
    EUSTD("BFA1-EUSTD", AppealType.EU, true, false, false, false, true),
    HUSTD("BFA1-HUSTD", AppealType.HU, true, false, false, false, true),
    DCSTD("BFA1-DCSTD", AppealType.DC, true, false, false, false, true),

    PASTV("BFA1-PASTV", AppealType.PA, false, false, true, false, true),
    EASTV("BFA1-EASTV", AppealType.EA, false, false, true, false, true),
    EUSTV("BFA1-EUSTV", AppealType.EU, false, false, true, false, true),
    HUSTV("BFA1-HUSTV", AppealType.HU, false, false, true, false, true),

    PASTF("BFA1-PASTF", AppealType.PA, false, true, false, false, true),
    RPSTF("BFA1-RPSTF", AppealType.RP, false, true, false, false, true),
    EASTF("BFA1-EASTF", AppealType.EA, false, true, false, false, true),
    EUSTF("BFA1-EUSTF", AppealType.EU, false, true, false, false, true),
    HUSTF("BFA1-HUSTF", AppealType.HU, false, true, false, false, true),
    DCSTF("BFA1-DCSTF", AppealType.DC, false, true, false, false, true),

    PASTVF("BFA1-PASTVF", AppealType.PA, false, true, true, false, true),
    EASTVF("BFA1-EASTVF", AppealType.EA, false, true, true, false, true),
    EUSTVF("BFA1-EUSTVF", AppealType.EU, false, true, true, false, true),
    HUSTVF("BFA1-HUSTVF", AppealType.HU, false, true, true, false, true);

    private static final Map<Key, CaseTypeValue> LOOKUP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(
                            value -> new Key(
                                    value.appealType,
                                    value.hasDeportation,
                                    value.isSuitableToFloat,
                                    value.isVirtualHearing,
                                    value.isAppellantInDetention,
                                    value.isStf24Weeks
                            ),
                            Function.identity(),
                            (a, b) -> {
                                throw new IllegalStateException(
                                        "Duplicate CaseTypeValue mapping: " + a + " and " + b
                                );
                            }
                    ));

    private final String value;
    private final AppealType appealType;
    private final boolean hasDeportation;
    private final boolean isSuitableToFloat;
    private final boolean isVirtualHearing;
    private final boolean isAppellantInDetention;
    private final boolean isStf24Weeks;

    CaseTypeValue(
            String value,
            AppealType appealType,
            boolean hasDeportation,
            boolean isSuitableToFloat,
            boolean isVirtualHearing,
            boolean isAppellantInDetention,
            boolean isStf24Weeks
    ) {
        this.value = value;
        this.appealType = appealType;
        this.hasDeportation = hasDeportation;
        this.isSuitableToFloat = isSuitableToFloat;
        this.isVirtualHearing = isVirtualHearing;
        this.isAppellantInDetention = isAppellantInDetention;
        this.isStf24Weeks = isStf24Weeks;
    }

    public static CaseTypeValue from(
            AppealType appealType,
            boolean hasDeportation,
            boolean isSuitableToFloat,
            boolean isVirtualHearing,
            boolean isAppellantInDetention,
            boolean isStf24Weeks
    ) {

        CaseTypeValue result = LOOKUP.get(
                new Key(
                        appealType,
                        hasDeportation,
                        isSuitableToFloat,
                        isVirtualHearing,
                        isAppellantInDetention,
                        isStf24Weeks
                )
        );

        if (result == null) {
            throw new IllegalArgumentException(
                    String.format(
                            "No CaseTypeValue found for %s [deportation=%s, float=%s, virtual=%s, detained=%s, stf=%s]",
                            appealType,
                            hasDeportation,
                            isSuitableToFloat,
                            isVirtualHearing,
                            isAppellantInDetention,
                            isStf24Weeks
                    )
            );
        }

        return result;
    }

    public String getValue() {
        return value;
    }

    public AppealType getAppealType() {
        return appealType;
    }

    public boolean hasDeportation() {
        return hasDeportation;
    }
    public boolean isSuitableToFloat(){
        return isSuitableToFloat;
    }
    public boolean isVirtualHearing(){
        return isVirtualHearing;
    }
    public boolean isAppellantInDetention(){
        return isAppellantInDetention;
    }
    public boolean isStf24Weeks(){
        return isStf24Weeks;
    }

    private record Key(
            AppealType appealType,
            boolean deportation,
            boolean floating,
            boolean virtual,
            boolean detained,
            boolean stf
    ) {
    }
}