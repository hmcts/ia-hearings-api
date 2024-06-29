package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DEPORTATION_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_APPEAL_SUITABLE_TO_FLOAT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.DCD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.DCF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.DCX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EAD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EAF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EAX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EUD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EUF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EUX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.HUD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.HUF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.HUX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.PAD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.PAF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.PAX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.RPD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.RPF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.RPX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.INTER;

import java.util.List;
import java.util.Objects;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

public class PayloadUtils {

    private PayloadUtils() {
    }

    public static List<CaseCategoryModel> getCaseCategoriesValue(AsylumCase asylumCase) {
        CaseTypeValue caseTypeValue = getCaseTypeValue(asylumCase);

        CaseCategoryModel caseCategoryCaseType = new CaseCategoryModel();
        caseCategoryCaseType.setCategoryType(CategoryType.CASE_TYPE);
        caseCategoryCaseType.setCategoryValue(caseTypeValue.getValue());
        caseCategoryCaseType.setCategoryParent("");

        CaseCategoryModel caseCategoryCaseSubType = new CaseCategoryModel();
        caseCategoryCaseSubType.setCategoryType(CategoryType.CASE_SUB_TYPE);
        caseCategoryCaseSubType.setCategoryValue(caseTypeValue.getValue());
        caseCategoryCaseSubType.setCategoryParent(caseTypeValue.getValue());

        return List.of(caseCategoryCaseType, caseCategoryCaseSubType);
    }

    private static CaseTypeValue getCaseTypeValue(AsylumCase asylumCase) {
        boolean hasDeportationOrder = asylumCase.read(DEPORTATION_ORDER_OPTIONS, YesOrNo.class)
            .map(deportation -> deportation == YesOrNo.YES)
            .orElse(false);

        boolean isSuitableToFloat = asylumCase.read(IS_APPEAL_SUITABLE_TO_FLOAT, YesOrNo.class)
            .map(deportation -> deportation == YesOrNo.YES)
            .orElse(false);

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new RequiredFieldMissingException("Appeal Type is a required field"));

        return getCaseTypeValueByAppealType(appealType, hasDeportationOrder, isSuitableToFloat);
    }

    private static CaseTypeValue getCaseTypeValueByAppealType(AppealType appealType,
                                                              boolean hasDeportationOrder,
                                                              boolean isSuitableToFloat) {
        return switch (appealType) {
            case HU -> getHuCaseTypeValue(hasDeportationOrder, isSuitableToFloat);
            case EA -> getEaCaseTypeValue(hasDeportationOrder, isSuitableToFloat);
            case EU -> getEuCaseTypeValue(hasDeportationOrder, isSuitableToFloat);
            case DC -> getDcCaseTypeValue(hasDeportationOrder, isSuitableToFloat);
            case PA -> getPaCaseTypeValue(hasDeportationOrder, isSuitableToFloat);
            case RP -> getRpCaseTypeValue(hasDeportationOrder, isSuitableToFloat);
        };
    }

    private static CaseTypeValue getHuCaseTypeValue(boolean hasDeportationOrder, boolean isSuitableToFloat) {
        if (hasDeportationOrder) {
            return HUD;
        } else if (isSuitableToFloat) {
            return HUF;
        } else {
            return HUX;
        }
    }

    private static CaseTypeValue getEaCaseTypeValue(boolean hasDeportationOrder, boolean isSuitableToFloat) {
        if (hasDeportationOrder) {
            return EAD;
        } else if (isSuitableToFloat) {
            return EAF;
        } else {
            return EAX;
        }
    }

    private static CaseTypeValue getEuCaseTypeValue(boolean hasDeportationOrder, boolean isSuitableToFloat) {
        if (hasDeportationOrder) {
            return EUD;
        } else if (isSuitableToFloat) {
            return EUF;
        } else {
            return EUX;
        }
    }

    private static CaseTypeValue getDcCaseTypeValue(boolean hasDeportationOrder, boolean isSuitableToFloat) {
        if (hasDeportationOrder) {
            return DCD;
        } else if (isSuitableToFloat) {
            return DCF;
        } else {
            return DCX;
        }
    }

    private static CaseTypeValue getPaCaseTypeValue(boolean hasDeportationOrder, boolean isSuitableToFloat) {
        if (hasDeportationOrder) {
            return PAD;
        } else if (isSuitableToFloat) {
            return PAF;
        } else {
            return PAX;
        }
    }

    private static CaseTypeValue getRpCaseTypeValue(boolean hasDeportationOrder, boolean isSuitableToFloat) {
        if (hasDeportationOrder) {
            return RPD;
        } else if (isSuitableToFloat) {
            return RPF;
        } else {
            return RPX;
        }
    }

    public static Integer getNumberOfPhysicalAttendees(List<PartyDetailsModel> partyDetails) {

        List<PartyDetailsModel> partiesWithChosenChannels = partyDetails.stream()
            .filter(party -> party.getIndividualDetails() != null
                             && party.getIndividualDetails().getPreferredHearingChannel() != null)
            .toList();

        if (partiesWithChosenChannels.isEmpty()) {
            return null;
        }

        int physicalAttendees = (int) partiesWithChosenChannels.stream()
            .filter(PayloadUtils::isInPersonAttendee)
            .count();

        // Plus one to include respondent (Home Office) which is an ORG type party
        return physicalAttendees > 0 ? physicalAttendees + 1 : 0;
    }

    private static boolean isInPersonAttendee(PartyDetailsModel party) {
        return party.getIndividualDetails() != null
               && Objects.equals(party.getIndividualDetails().getPreferredHearingChannel(), INTER.name());
    }
}
