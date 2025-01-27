package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseLink;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ContactPreference;

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
    LISTING_LENGTH(
        "listingLength", new TypeReference<HoursMinutes>() {}),
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
    LEGAL_REP_COMPANY("legalRepCompany", new TypeReference<String>(){}),
    LEGAL_REP_COMPANY_NAME("legalRepCompanyName", new TypeReference<String>(){}),
    LOCAL_AUTHORITY_POLICY(
        "localAuthorityPolicy", new TypeReference<OrganisationPolicy>(){}),
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
    CHANGE_HEARING_VENUE(
        "changeHearingVenue", new TypeReference<String>(){}),
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
        "manualCanHearingRequired", new TypeReference<YesOrNo>(){}),
    MANUAL_UPDATE_HEARING_REQUIRED(
        "manualUpdHearingRequired", new TypeReference<YesOrNo>(){}),
    MANUAL_CREATE_HEARINGS_REQUIRED(
        "manualCreHearingRequired", new TypeReference<YesOrNo>(){}),
    HEARING_ADJOURNMENT_WHEN(
        "hearingAdjournmentWhen", new TypeReference<HearingAdjournmentDay>(){}),
    RELIST_CASE_IMMEDIATELY(
        "relistCaseImmediately", new TypeReference<YesOrNo>(){}),
    UPDATE_HMC_REQUEST_SUCCESS(
        "updateHmcRequestSuccess", new TypeReference<YesOrNo>() {}),
    HEARING_REASON_TO_CANCEL(
        "hearingReasonToCancel", new TypeReference<DynamicList>(){}),
    HEARING_REASON_TO_UPDATE(
        "hearingReasonToUpdate", new TypeReference<DynamicList>(){}),
    DEPORTATION_ORDER_OPTIONS(
        "deportationOrderOptions", new TypeReference<YesOrNo>(){}),
    APPEAL_TYPE(
        "appealType", new TypeReference<AppealType>(){}),
    APPELLANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS(
        "appellantInterpreterSpokenLanguageBookingStatus", new TypeReference<InterpreterBookingStatus>() {}),

    APPELLANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS(
        "appellantInterpreterSignLanguageBookingStatus", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1(
        "witnessInterpreterSpokenLanguageBookingStatus1", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2(
        "witnessInterpreterSpokenLanguageBookingStatus2", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3(
        "witnessInterpreterSpokenLanguageBookingStatus3", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4(
        "witnessInterpreterSpokenLanguageBookingStatus4", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_5(
        "witnessInterpreterSpokenLanguageBookingStatus5", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_6(
        "witnessInterpreterSpokenLanguageBookingStatus6", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_7(
        "witnessInterpreterSpokenLanguageBookingStatus7", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_8(
        "witnessInterpreterSpokenLanguageBookingStatus8", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_9(
        "witnessInterpreterSpokenLanguageBookingStatus9", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_10(
        "witnessInterpreterSpokenLanguageBookingStatus10", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1(
        "witnessInterpreterSignLanguageBookingStatus1", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2(
        "witnessInterpreterSignLanguageBookingStatus2", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3(
        "witnessInterpreterSignLanguageBookingStatus3", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4(
        "witnessInterpreterSignLanguageBookingStatus4", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_5(
        "witnessInterpreterSignLanguageBookingStatus5", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_6(
        "witnessInterpreterSignLanguageBookingStatus6", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_7(
        "witnessInterpreterSignLanguageBookingStatus7", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_8(
        "witnessInterpreterSignLanguageBookingStatus8", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_9(
        "witnessInterpreterSignLanguageBookingStatus9", new TypeReference<InterpreterBookingStatus>() {}),

    WITNESS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_10(
        "witnessInterpreterSignLanguageBookingStatus10", new TypeReference<InterpreterBookingStatus>() {}),

    NEXT_HEARING_DATE(
        "nextHearingDate", new TypeReference<String>(){}),

    NEXT_HEARING_DATE_FIXED(
        "nextHearingDateFixed", new TypeReference<String>(){}),

    NEXT_HEARING_DATE_RANGE_EARLIEST(
        "nextHearingDateRangeEarliest", new TypeReference<String>(){}),

    NEXT_HEARING_DATE_RANGE_LATEST(
        "nextHearingDateRangeLatest", new TypeReference<String>(){}),
    NEXT_HEARING_FORMAT(
        "nextHearingFormat", new TypeReference<DynamicList>(){}),
    NEXT_HEARING_DURATION(
        "nextHearingDuration", new TypeReference<String>(){}),
    NEXT_HEARING_VENUE(
        "nextHearingVenue", new TypeReference<DynamicList>(){}),

    IS_APPEAL_SUITABLE_TO_FLOAT(
        "isAppealSuitableToFloat", new TypeReference<YesOrNo>() {}),

    CASE_LINKS(
        "caseLinks", new TypeReference<List<IdValue<CaseLink>>>(){}),

    DECISION_HEARING_FEE_OPTION(
        "decisionHearingFeeOption", new TypeReference<String>(){}),

    MAKE_AN_APPLICATION_DECISION_REASON(
        "makeAnApplicationDecisionReason", new TypeReference<String>(){}),

    CHANGE_ORGANISATION_REQUEST_FIELD(
        "changeOrganisationRequestField", new TypeReference<ChangeOrganisationRequest>(){}),

    SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK(
        "shouldTriggerReviewInterpreterTask", new TypeReference<YesOrNo>(){}),

    DECISION_WITHOUT_HEARING_LISTED(
        "decisionWithoutHearingListed", new TypeReference<YesOrNo>(){}),

    IS_HEARING_LINKED("isHearingLinked", new TypeReference<YesOrNo>(){}),

    REQUEST_HEARING_CHANNEL(
        "requestHearingChannel", new TypeReference<DynamicList>(){}),

    HEARING_LOCATION("hearingLocation", new TypeReference<DynamicList>(){}),

    REQUEST_HEARING_LENGTH(
        "requestHearingLength", new TypeReference<String>() {}),

    REQUEST_HEARING_DATE_1(
        "requestHearingDate1", new TypeReference<String>(){}),

    ADDITIONAL_INSTRUCTIONS_DESCRIPTION("additionalInstructionsDescription", new TypeReference<String>(){}),

    LISTING_LOCATION(
        "listingLocation", new TypeReference<DynamicList>(){}),

    IS_REMOTE_HEARING(
        "isRemoteHearing", new TypeReference<YesOrNo>(){}),

    IS_CASE_USING_LOCATION_REF_DATA(
        "isCaseUsingLocationRefData", new TypeReference<YesOrNo>(){}),

    IS_DECISION_WITHOUT_HEARING("isDecisionWithoutHearing", new TypeReference<YesOrNo>(){}),

    CONTACT_PREFERENCE("contactPreference", new TypeReference<ContactPreference>(){}),

    NEXT_HEARING_DETAILS("nextHearingDetails", new TypeReference<NextHearingDetails>(){}),

    CASE_MANAGEMENT_LOCATION_REF_DATA(
        "caseManagementLocationRefData", new TypeReference<CaseManagementLocationRefData>(){}),

    IS_ADMIN(
        "isAdmin", new TypeReference<YesOrNo>() {}),

    APPELLANTS_REPRESENTATION(
        "appellantsRepresentation", new TypeReference<YesOrNo>(){}),

    LEGAL_REP_GIVEN_NAME(
        "legalRepGivenName", new TypeReference<String>(){}),

    LEGAL_REP_FAMILY_NAME_PAPER_J(
        "legalRepFamilyNamePaperJ", new TypeReference<String>(){}),

    LEGAL_REP_EMAIL(
        "legalRepEmail", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY_PAPER_J(
        "legalRepCompanyPaperJ", new TypeReference<String>(){}),

    CURRENT_HEARING_ID(
        "currentHearingId", new TypeReference<String>() {});

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
