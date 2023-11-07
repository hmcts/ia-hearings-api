package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

public enum AsylumCaseFieldDefinition {

    JOURNEY_TYPE(
        "journeyType", new TypeReference<String>(){}),
    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>(){}),
    GWF_REFERENCE_NUMBER(
        "gwfReferenceNumber", new TypeReference<String>(){}),
    CASE_MANAGEMENT_LOCATION(
        "caseManagementLocation", new TypeReference<CaseManagementLocation>(){}),
    HEARING_CHANNEL(
        "hearingChannel", new TypeReference<DynamicList>(){}),
    HMCTS_CASE_NAME_INTERNAL(
        "hmctsCaseNameInternal", new TypeReference<String>() {}),
    LIST_CASE_HEARING_LENGTH(
        "listCaseHearingLength", new TypeReference<String>() {}),
    LIST_CASE_HEARING_CENTRE(
        "listCaseHearingCentre", new TypeReference<HearingCentre>(){}),
    APPELLANT_LEVEL_FLAGS("appellantLevelFlags", new TypeReference<StrategicCaseFlag>() {
    }),
    WITNESS_LEVEL_FLAGS(
        "witnessLevelFlags", new TypeReference<List<PartyFlagIdValue>>() {}),
    CASE_FLAGS(
        "caseFlags", new TypeReference<StrategicCaseFlag>(){}),
    LEGAL_REP_NAME(
        "legalRepName", new TypeReference<String>(){}),
    LEGAL_REP_FAMILY_NAME(
        "legalRepFamilyName", new TypeReference<String>(){}),
    LEGAL_REPRESENTATIVE_EMAIL_ADDRESS(
        "legalRepresentativeEmailAddress", new TypeReference<String>(){}),
    LEGAL_REP_MOBILE_PHONE_NUMBER(
        "legalRepMobilePhoneNumber", new TypeReference<String>(){}),
    LEGAL_REP_COMPANY_NAME("legalRepCompanyName", new TypeReference<String>(){}),
    APPELLANT_TITLE(
        "appellantTitle", new TypeReference<String>(){}),
    APPELLANT_GIVEN_NAMES(
        "appellantGivenNames", new TypeReference<String>(){}),
    APPELLANT_FAMILY_NAME(
        "appellantFamilyName", new TypeReference<String>(){}),
    APPELLANT_EMAIL_ADDRESS("appellantEmailAddress", new TypeReference<String>(){}),
    EMAIL("email", new TypeReference<String>(){}),
    APPELLANT_PHONE_NUMBER("appellantPhoneNumber", new TypeReference<String>(){}),
    MOBILE_NUMBER("mobileNumber", new TypeReference<String>(){}),
    APPELLANT_NAME_FOR_DISPLAY(
        "appellantNameForDisplay", new TypeReference<String>(){}),
    ADDITIONAL_INSTRUCTIONS_TRIBUNAL_RESPONSE(
        "additionalInstructionsTribunalResponse", new TypeReference<String>(){}),
    DATES_TO_AVOID(
        "datesToAvoid", new TypeReference<List<IdValue<DatesToAvoid>>>(){}),
    APPELLANT_IN_UK(
        "appellantInUk", new TypeReference<YesOrNo>() {}),
    S94B_STATUS(
        "s94bStatus", new TypeReference<YesOrNo>(){}),
    WITNESS_DETAILS(
        "witnessDetails", new TypeReference<List<IdValue<WitnessDetails>>>() {}),
    HAS_SPONSOR(
        "hasSponsor", new TypeReference<YesOrNo>(){}),
    SPONSOR_GIVEN_NAMES(
        "sponsorGivenNames", new TypeReference<String>(){}),

    SPONSOR_FAMILY_NAME(
        "sponsorFamilyName", new TypeReference<String>(){}),

    SPONSOR_EMAIL(
        "sponsorEmail", new TypeReference<String>(){}),

    SPONSOR_MOBILE_NUMBER(
        "sponsorMobileNumber", new TypeReference<String>(){}),

    VULNERABILITIES_TRIBUNAL_RESPONSE(
        "vulnerabilitiesTribunalResponse", new TypeReference<String>() {}),

    MULTIMEDIA_TRIBUNAL_RESPONSE(
        "multimediaTribunalResponse", new TypeReference<String>() {}),

    ADDITIONAL_TRIBUNAL_RESPONSE(
        "additionalTribunalResponse", new TypeReference<String>() {}),

    SINGLE_SEX_COURT_TYPE(
        "singleSexCourtType", new TypeReference<String>() {}),

    SINGLE_SEX_COURT_TRIBUNAL_RESPONSE(
        "singleSexCourtTribunalResponse", new TypeReference<String>() {}),

    IS_VULNERABILITIES_ALLOWED(
        "isVulnerabilitiesAllowed", new TypeReference<String>() {}),

    IS_MULTIMEDIA_ALLOWED(
        "isMultimediaAllowed", new TypeReference<String>() {}),

    IS_ADDITIONAL_ADJUSTMENTS_ALLOWED(
        "isAdditionalAdjustmentsAllowed", new TypeReference<String>() {}),

    IS_SINGLE_SEX_COURT_ALLOWED(
        "isSingleSexCourtAllowed", new TypeReference<String>() {}),

    INTERPRETER_DETAILS(
        "interpreterDetails", new TypeReference<List<IdValue<InterpreterDetails>>>() {}),

    APPELLANT_PARTY_ID(
        "appellantPartyId", new TypeReference<String>() {}),

    LEGAL_REP_INDIVIDUAL_PARTY_ID(
        "legalRepIndividualPartyId", new TypeReference<String>() {}),

    LEGAL_REP_ORGANISATION_PARTY_ID(
        "legalRepOrganisationPartyId", new TypeReference<String>() {}),

    SPONSOR_PARTY_ID(
        "sponsorPartyId", new TypeReference<String>() {}),

    ARIA_LISTING_REFERENCE(
        "ariaListingReference",  new TypeReference<String>(){}),

    LIST_CASE_HEARING_DATE(
        "listCaseHearingDate", new TypeReference<String>(){}),

    CHANGE_HEARINGS(
        "changeHearings", new TypeReference<DynamicList>(){}),
    CHANGE_HEARING_TYPE(
        "changeHearingType", new TypeReference<String>(){}),
    CHANGE_HEARING_TYPE_YES_NO(
        "changeHearingTypeYesNo", new TypeReference<String>(){}),
    CHANGE_HEARING_LOCATION(
        "changeHearingLocation", new TypeReference<String>(){}),
    CHANGE_HEARING_LOCATION_YES_NO(
        "changeHearingLocationYesNo", new TypeReference<String>(){}),
    CHANGE_HEARING_DATE(
        "changeHearingDate", new TypeReference<String>(){}),
    CHANGE_HEARING_DURATION(
        "changeHearingDuration", new TypeReference<String>(){}),
    CHANGE_HEARING_DURATION_YES_NO(
        "changeHearingDurationYesNo", new TypeReference<String>(){}),
    CHANGE_HEARING_UPDATE_REASON(
        "changeHearingUpdateReason", new TypeReference<DynamicList>(){}),
    CHANGE_HEARING_DATE_YES_NO(
        "changeHearingDateYesNo", new TypeReference<String>(){}),
    CHANGE_HEARING_DATE_TYPE(
        "changeHearingDateType", new TypeReference<String>(){}),
    CHANGE_HEARING_DATE_RANGE_EARLIEST(
        "changeHearingDateRangeEarliest", new TypeReference<String>(){}),
    CHANGE_HEARING_DATE_RANGE_LATEST(
        "changeHearingDateRangeLatest", new TypeReference<String>(){}),
    ADJOURNMENT_DETAILS_HEARING(
        "adjournmentDetailsHearing", new TypeReference<DynamicList>(){}),
    END_APPEAL_OUTCOME(
        "endAppealOutcome", new TypeReference<String>(){}),
    IS_INTEGRATED(
        "isIntegrated", new TypeReference<YesOrNo>(){}),
    MANUAL_CANCEL_HEARINGS_REQUIRED(
        "manualCancelHearingsRequired", new TypeReference<YesOrNo>(){}),
    MANUAL_UPDATE_HEARING_REQUIRED(
        "manualUpdateHearingsRequired", new TypeReference<YesOrNo>(){}),
    HEARING_ADJOURNMENT_WHEN(
        "hearingAdjournmentWhen", new TypeReference<HearingAdjournmentDay>(){}),
    RELIST_CASE_IMMEDIATELY(
        "relistCaseImmediately", new TypeReference<YesOrNo>(){}),
    UPDATE_HMC_REQUEST_SUCCESS(
        "updateHmcRequestSuccess", new TypeReference<YesOrNo>() {}),
    HEARING_CANCELLATION_REASON(
        "hearingCancellationReason", new TypeReference<String>(){})
    ;

    private final String value;
    private final TypeReference typeReference;

    AsylumCaseFieldDefinition(String value, TypeReference typeReference) {
        this.value = value;
        this.typeReference = typeReference;
    }

    public String value() {
        return value;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }
}
