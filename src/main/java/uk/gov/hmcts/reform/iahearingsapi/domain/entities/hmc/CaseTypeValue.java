package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumMap;

@Slf4j
@Getter
public enum CaseTypeValue {

    RPX("BFA1-RPX", AppealType.RP, Category.DEFAULT),
    PAX("BFA1-PAX", AppealType.PA, Category.DEFAULT),
    EAX("BFA1-EAX", AppealType.EA, Category.DEFAULT),
    EUX("BFA1-EUX", AppealType.EU, Category.DEFAULT),
    HUX("BFA1-HUX", AppealType.HU, Category.DEFAULT),
    DCX("BFA1-DCX", AppealType.DC, Category.DEFAULT),

    RPD("BFA1-RPD", AppealType.RP, Category.DEPORT),
    PAD("BFA1-PAD", AppealType.PA, Category.DEPORT),
    EAD("BFA1-EAD", AppealType.EA, Category.DEPORT),
    EUD("BFA1-EUD", AppealType.EU, Category.DEPORT),
    HUD("BFA1-HUD", AppealType.HU, Category.DEPORT),
    DCD("BFA1-DCD", AppealType.DC, Category.DEPORT),

    PAF("BFA1-PAF", AppealType.PA, Category.FLOAT),
    RPF("BFA1-RPF", AppealType.RP, Category.FLOAT),
    EAF("BFA1-EAF", AppealType.EA, Category.FLOAT),
    EUF("BFA1-EUF", AppealType.EU, Category.FLOAT),
    HUF("BFA1-HUF", AppealType.HU, Category.FLOAT),
    DCF("BFA1-DCF", AppealType.DC, Category.FLOAT),

    PAV("BFA1-PAV", AppealType.PA, Category.VIRTUAL),
    EAV("BFA1-EAV", AppealType.EA, Category.VIRTUAL),
    EUV("BFA1-EUV", AppealType.EU, Category.VIRTUAL),
    HUV("BFA1-HUV", AppealType.HU, Category.VIRTUAL),

    PAVF("BFA1-PAVF", AppealType.PA, Category.VIRTUAL_FLOAT),
    EAVF("BFA1-EAVF", AppealType.EA, Category.VIRTUAL_FLOAT),
    EUVF("BFA1-EUVF", AppealType.EU, Category.VIRTUAL_FLOAT),
    HUVF("BFA1-HUVF", AppealType.HU, Category.VIRTUAL_FLOAT),

    PADEX("BFA1-PADEX", AppealType.PA, Category.DETAINED),
    RPDEX("BFA1-RPDEX", AppealType.RP, Category.DETAINED),
    EADEX("BFA1-EADEX", AppealType.EA, Category.DETAINED),
    EUDEX("BFA1-EUDEX", AppealType.EU, Category.DETAINED),
    HUDEX("BFA1-HUDEX", AppealType.HU, Category.DETAINED),
    DCDEX("BFA1-DCDEX", AppealType.DC, Category.DETAINED),

    PADED("BFA1-PADED", AppealType.PA, Category.DETAINED_DEPORT),
    RPDED("BFA1-RPDED", AppealType.RP, Category.DETAINED_DEPORT),
    EADED("BFA1-EADED", AppealType.EA, Category.DETAINED_DEPORT),
    EUDED("BFA1-EUDED", AppealType.EU, Category.DETAINED_DEPORT),
    HUDED("BFA1-HUDED", AppealType.HU, Category.DETAINED_DEPORT),
    DCDED("BFA1-DCDED", AppealType.DC, Category.DETAINED_DEPORT),

    PASTX("BFA1-PASTX", AppealType.PA, Category.STF_DEFAULT),
    RPSTX("BFA1-RPSTX", AppealType.RP, Category.STF_DEFAULT),
    EASTX("BFA1-EASTX", AppealType.EA, Category.STF_DEFAULT),
    EUSTX("BFA1-EUSTX", AppealType.EU, Category.STF_DEFAULT),
    HUSTX("BFA1-HUSTX", AppealType.HU, Category.STF_DEFAULT),
    DCSTX("BFA1-DCSTX", AppealType.DC, Category.STF_DEFAULT),

    PASTD("BFA1-PASTD", AppealType.PA, Category.STF_DEPORT),
    RPSTD("BFA1-RPSTD", AppealType.RP, Category.STF_DEPORT),
    EASTD("BFA1-EASTD", AppealType.EA, Category.STF_DEPORT),
    EUSTD("BFA1-EUSTD", AppealType.EU, Category.STF_DEPORT),
    HUSTD("BFA1-HUSTD", AppealType.HU, Category.STF_DEPORT),
    DCSTD("BFA1-DCSTD", AppealType.DC, Category.STF_DEPORT),

    PASTV("BFA1-PASTV", AppealType.PA, Category.STF_VIRTUAL),
    EASTV("BFA1-EASTV", AppealType.EA, Category.STF_VIRTUAL),
    EUSTV("BFA1-EUSTV", AppealType.EU, Category.STF_VIRTUAL),
    HUSTV("BFA1-HUSTV", AppealType.HU, Category.STF_VIRTUAL),

    PASTF("BFA1-PASTF", AppealType.PA, Category.STF_FLOAT),
    RPSTF("BFA1-RPSTF", AppealType.RP, Category.STF_FLOAT),
    EASTF("BFA1-EASTF", AppealType.EA, Category.STF_FLOAT),
    EUSTF("BFA1-EUSTF", AppealType.EU, Category.STF_FLOAT),
    HUSTF("BFA1-HUSTF", AppealType.HU, Category.STF_FLOAT),
    DCSTF("BFA1-DCSTF", AppealType.DC, Category.STF_FLOAT),

    PASTVF("BFA1-PASTVF", AppealType.PA, Category.STF_VIRTUAL_FLOAT),
    EASTVF("BFA1-EASTVF", AppealType.EA, Category.STF_VIRTUAL_FLOAT),
    EUSTVF("BFA1-EUSTVF", AppealType.EU, Category.STF_VIRTUAL_FLOAT),
    HUSTVF("BFA1-HUSTVF", AppealType.HU, Category.STF_VIRTUAL_FLOAT);

    private final String value;
    private final AppealType appealType;
    private final Category category;

    private enum Category {
        DEFAULT,
        DEPORT,
        FLOAT,
        VIRTUAL,
        VIRTUAL_FLOAT,
        DETAINED,
        DETAINED_DEPORT,
        STF_DEFAULT,
        STF_DEPORT,
        STF_VIRTUAL,
        STF_FLOAT,
        STF_VIRTUAL_FLOAT
    }

    CaseTypeValue(String value,
                  AppealType appealType,
                  Category category) {
        this.value = value;
        this.appealType = appealType;
        this.category = category;
    }

    private static final EnumMap<AppealType, EnumMap<Category, CaseTypeValue>> LOOKUP =
        new EnumMap<>(AppealType.class);

    static {
        for (AppealType appealType : AppealType.values()) {
            LOOKUP.put(appealType, new EnumMap<>(Category.class));
        }

        for (CaseTypeValue value : values()) {
            LOOKUP.get(value.appealType).put(value.category, value);
        }
    }

    public static CaseTypeValue from(
        AppealType appealType,
        boolean isVirtualHearing,
        boolean isSuitableToFloat,
        boolean hasDeportation,
        boolean isAppellantInDetention,
        boolean isStf24Weeks
    ) {
        CaseTypeValue value = LOOKUP.get(appealType)
            .get(resolveCategory(
                isVirtualHearing,
                isSuitableToFloat,
                hasDeportation,
                isAppellantInDetention,
                isStf24Weeks
            ));
        return value == null ? getDefaultCaseTypeValue(appealType, isStf24Weeks) : value;
    }

    private static CaseTypeValue getDefaultCaseTypeValue(AppealType appealType, boolean isStf24Weeks) {
        return LOOKUP.get(appealType).get(isStf24Weeks ? Category.STF_DEFAULT : Category.DEFAULT);
    }

    private static Category resolveCategory(
        boolean isVirtual,
        boolean isFloat,
        boolean deportation,
        boolean detained,
        boolean stf
    ) {
        if (stf) {
            if (isVirtual) {
                return isFloat
                    ? Category.STF_VIRTUAL_FLOAT
                    : Category.STF_VIRTUAL;
            }

            if (deportation) {
                return Category.STF_DEPORT;
            }

            return isFloat
                ? Category.STF_FLOAT
                : Category.STF_DEFAULT;
        }

        if (isVirtual) {
            return isFloat
                ? Category.VIRTUAL_FLOAT
                : Category.VIRTUAL;
        }

        if (deportation) {
            return detained
                ? Category.DETAINED_DEPORT
                : Category.DEPORT;
        }

        if (detained) {
            return isFloat
                ? Category.DEFAULT
                : Category.DETAINED;
        }

        return isFloat
            ? Category.FLOAT
            : Category.DEFAULT;
    }
}
