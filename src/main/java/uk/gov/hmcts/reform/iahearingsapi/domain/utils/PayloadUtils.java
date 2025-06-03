package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DEPORTATION_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_APPEAL_SUITABLE_TO_FLOAT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.DCD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.DCDED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.DCDEX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.DCF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.DCX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EAD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EADED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EADEX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EAF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EAX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EUD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EUDED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EUDEX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EUF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.EUX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.HUD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.HUDED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.HUDEX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.HUF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.HUX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.PAD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.PADED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.PADEX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.PAF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.PAX;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.RPD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.RPDED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue.RPDEX;
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

        boolean appellantInDetention = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)
            .map(detention -> detention == YesOrNo.YES)
            .orElse(false);

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new RequiredFieldMissingException("Appeal Type is a required field"));

        return switch (appealType) {
            case HU -> {
                if (!hasDeportationOrder && !isSuitableToFloat && !appellantInDetention) {
                    yield HUX;
                } else if (hasDeportationOrder && !appellantInDetention) {
                    yield HUD;
                } else if (!hasDeportationOrder && isSuitableToFloat && !appellantInDetention) {
                    yield HUF;
                } else if (!hasDeportationOrder && !isSuitableToFloat && appellantInDetention) {
                    yield HUDEX;
                } else {
                    yield HUDED;
                }
                // the logic for HUDED is (hasDeportationOrder && !isSuitableToFloat && appellantInDetention) but the
                // compiler requires that we handle all cases so else is used
            }
            case EA -> {
                if (!hasDeportationOrder && !isSuitableToFloat && !appellantInDetention) {
                    yield EAX;
                } else if (hasDeportationOrder && !appellantInDetention) {
                    yield EAD;
                } else if (!hasDeportationOrder && isSuitableToFloat && !appellantInDetention) {
                    yield EAF;
                } else if (!hasDeportationOrder && !isSuitableToFloat && appellantInDetention) {
                    yield EADEX;
                } else {
                    yield EADED;
                }
                // the logic for EADED is (hasDeportationOrder && !isSuitableToFloat && appellantInDetention) but the
                // compiler requires that we handle all cases so else is used
            }
            case EU -> {
                if (!hasDeportationOrder && !isSuitableToFloat && !appellantInDetention) {
                    yield EUX;
                } else if (hasDeportationOrder && !appellantInDetention) {
                    yield EUD;
                } else if (!hasDeportationOrder && isSuitableToFloat && !appellantInDetention) {
                    yield EUF;
                } else if (!hasDeportationOrder && !isSuitableToFloat && appellantInDetention) {
                    yield EUDEX;
                } else {
                    yield EUDED;
                }
                // the logic for EUDED is (hasDeportationOrder && !isSuitableToFloat && appellantInDetention) but the
                // compiler requires that we handle all cases so else is used
            }
            case DC -> {
                if (!hasDeportationOrder && !isSuitableToFloat && !appellantInDetention) {
                    yield DCX;
                } else if (hasDeportationOrder && !appellantInDetention) {
                    yield DCD;
                } else if (!hasDeportationOrder && isSuitableToFloat && !appellantInDetention) {
                    yield DCF;
                } else if (!hasDeportationOrder && !isSuitableToFloat && appellantInDetention) {
                    yield DCDEX;
                } else {
                    yield DCDED;
                }
                // the logic for DCDED is (hasDeportationOrder && !isSuitableToFloat && appellantInDetention) but the
                // compiler requires that we handle all cases so else is used
            }
            case PA -> {
                if (!hasDeportationOrder && !isSuitableToFloat && !appellantInDetention) {
                    yield PAX;
                } else if (hasDeportationOrder && !appellantInDetention) {
                    yield PAD;
                } else if (!hasDeportationOrder && isSuitableToFloat && !appellantInDetention) {
                    yield PAF;
                } else if (!hasDeportationOrder && !isSuitableToFloat && appellantInDetention) {
                    yield PADEX;
                } else {
                    yield PADED;
                }
                // the logic for PADED is (hasDeportationOrder && !isSuitableToFloat && appellantInDetention) but the
                // compiler requires that we handle all cases so else is used
            }
            case RP -> {
                if (!hasDeportationOrder && !isSuitableToFloat && !appellantInDetention) {
                    yield RPX;
                } else if (hasDeportationOrder && !appellantInDetention) {
                    yield RPD;
                } else if (!hasDeportationOrder && isSuitableToFloat && !appellantInDetention) {
                    yield RPF;
                } else if (!hasDeportationOrder && !isSuitableToFloat && appellantInDetention) {
                    yield RPDEX;
                } else {
                    yield RPDED;
                }
                // the logic for RPDED is (hasDeportationOrder && !isSuitableToFloat && appellantInDetention) but the
                // compiler requires that we handle all cases so else is used
            }
        };
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
