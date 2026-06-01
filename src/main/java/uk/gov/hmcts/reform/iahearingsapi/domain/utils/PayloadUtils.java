package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.*;
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

        boolean isVirtualHearing = asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)
            .map(virtual -> virtual == YesOrNo.YES)
            .orElse(false);

        boolean isStf24Weeks = asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)
            .map(stf -> stf == YesOrNo.YES)
            .orElse(false);

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new RequiredFieldMissingException("Appeal Type is a required field"));

        return CaseTypeValue.from(
                appealType,
                hasDeportationOrder,
                isSuitableToFloat,
                isVirtualHearing,
                appellantInDetention,
                isStf24Weeks
        );

    }

    public static Integer getNumberOfPhysicalAttendees(List<PartyDetailsModel> partyDetails) {

        long physicalAttendees = partyDetails.stream()
                .filter(PayloadUtils::isInPersonAttendee)
                .count();

        boolean hasAnyChannelSelection = partyDetails.stream()
                .anyMatch(party ->
                        party.getIndividualDetails() != null
                                && party.getIndividualDetails().getPreferredHearingChannel() != null);

        if (!hasAnyChannelSelection) {
            return null;
        }

        return physicalAttendees > 0
                ? (int) physicalAttendees + 1
                : 0;
    }

    private static boolean isInPersonAttendee(PartyDetailsModel party) {
        return party.getIndividualDetails() != null
                && Objects.equals(party.getIndividualDetails().getPreferredHearingChannel(), INTER.name());
    }
}
