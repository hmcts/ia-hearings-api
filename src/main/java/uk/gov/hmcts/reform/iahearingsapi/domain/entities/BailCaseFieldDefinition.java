package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailPartyFlagIdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

import java.util.List;

public enum BailCaseFieldDefinition {
    CASE_NAME_HMCTS_INTERNAL(
        "caseNameHmctsInternal", new TypeReference<String>() {}),
    APPLICANT_FULL_NAME(
        "applicantFullName", new TypeReference<String>(){}),
    APPLICANT_GIVEN_NAMES(
        "applicantGivenNames", new TypeReference<String>() {}),
    APPLICANT_FAMILY_NAME(
        "applicantFamilyName", new TypeReference<String>() {}),
    CASE_FLAGS(
        "caseFlags", new TypeReference<BailStrategicCaseFlag>(){}),
    APPELLANT_LEVEL_FLAGS(
        "appellantLevelFlags", new TypeReference<BailStrategicCaseFlag>() {}),
    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>(){}),
    FCS_LEVEL_FLAGS(
        "fcsLevelFlags", new TypeReference<List<BailPartyFlagIdValue>>() {}),
    APPLICANT_DISABILITY1(
        "applicantDisability1", new TypeReference<YesOrNo>(){}),
    APPLICANT_DISABILITY_DETAILS(
        "applicantDisabilityDetails", new TypeReference<String>(){}),
    APPLICANT_DOCUMENTS_WITH_METADATA(
        "applicantDocumentsWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    VIDEO_HEARING1(
        "videoHearing1", new TypeReference<YesOrNo>(){}),
    APPLICANT_PARTY_ID(
        "applicantPartyId", new TypeReference<String>(){}),
    LEGAL_REP_INDIVIDUAL_PARTY_ID(
        "legalRepIndividualPartyId", new TypeReference<String>(){}),
    LEGAL_REP_ORGANISATION_PARTY_ID(
        "legalRepOrganisationPartyId", new TypeReference<String>(){}),
    SUPPORTER_1_PARTY_ID(
        "supporter1PartyId", new TypeReference<String>(){}),
    SUPPORTER_2_PARTY_ID(
        "supporter2PartyId", new TypeReference<String>(){}),
    SUPPORTER_3_PARTY_ID(
        "supporter3PartyId", new TypeReference<String>(){}),
    SUPPORTER_4_PARTY_ID(
        "supporter4PartyId", new TypeReference<String>(){}),
    IS_LEGALLY_REPRESENTED_FOR_FLAG(
        "isLegallyRepresentedForFlag", new TypeReference<YesOrNo>() {}),
    LEGAL_REP_EMAIL(
        "legalRepEmail", new TypeReference<String>(){}),
    LEGAL_REP_NAME(
        "legalRepName", new TypeReference<String>(){}),
    LEGAL_REP_FAMILY_NAME(
        "legalRepFamilyName", new TypeReference<String>(){}),
    LEGAL_REP_PHONE(
        "legalRepPhone", new TypeReference<String>(){}),
    LEGAL_REP_COMPANY(
        "legalRepCompany", new TypeReference<String>(){}),
    SUPPORTER_GIVEN_NAMES(
        "supporterGivenNames", new TypeReference<String>(){}),
    SUPPORTER_FAMILY_NAMES(
        "supporterFamilyNames", new TypeReference<String>(){}),
    SUPPORTER_2_GIVEN_NAMES(
        "supporter2GivenNames", new TypeReference<String>(){}),
    SUPPORTER_2_FAMILY_NAMES(
        "supporter2FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_3_GIVEN_NAMES(
        "supporter3GivenNames", new TypeReference<String>(){}),
    SUPPORTER_3_FAMILY_NAMES(
        "supporter3FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_4_GIVEN_NAMES(
        "supporter4GivenNames", new TypeReference<String>(){}),
    SUPPORTER_4_FAMILY_NAMES(
        "supporter4FamilyNames", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER(
        "hasFinancialCondSupporter", new TypeReference<YesOrNo>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_2(
        "hasFinancialCondSupporter2", new TypeReference<YesOrNo>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_3(
        "hasFinancialCondSupporter3", new TypeReference<YesOrNo>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_4(
        "hasFinancialCondSupporter4", new TypeReference<YesOrNo>(){}),
    SUPPORTER_TELEPHONE_NUMBER_1(
        "supporterTelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_MOBILE_NUMBER_1(
        "supporterMobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_EMAIL_ADDRESS_1(
        "supporterEmailAddress1", new TypeReference<String>(){}),
    SUPPORTER_2_TELEPHONE_NUMBER_1(
        "supporter2TelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_2_MOBILE_NUMBER_1(
        "supporter2MobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_2_EMAIL_ADDRESS_1(
        "supporter2EmailAddress1", new TypeReference<String>(){}),
    SUPPORTER_3_TELEPHONE_NUMBER_1(
        "supporter3TelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_3_MOBILE_NUMBER_1(
        "supporter3MobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_3_EMAIL_ADDRESS_1(
        "supporter3EmailAddress1", new TypeReference<String>(){}),
    SUPPORTER_4_TELEPHONE_NUMBER_1(
        "supporter4TelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_4_MOBILE_NUMBER_1(
        "supporter4MobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_4_EMAIL_ADDRESS_1(
        "supporter4EmailAddress1", new TypeReference<String>(){}),
    APPLICANT_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS(
        "applicantInterpreterSpokenLanguageBookingStatus", new TypeReference<InterpreterBookingStatus>(){}),
    APPLICANT_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS(
        "applicantInterpreterSignLanguageBookingStatus", new TypeReference<InterpreterBookingStatus>(){}),
    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_1(
        "fcsInterpreterSpokenLanguageBookingStatus1", new TypeReference<InterpreterBookingStatus>(){}),
    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_2(
        "fcsInterpreterSpokenLanguageBookingStatus2", new TypeReference<InterpreterBookingStatus>(){}),
    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_3(
        "fcsInterpreterSpokenLanguageBookingStatus3", new TypeReference<InterpreterBookingStatus>(){}),
    FCS_INTERPRETER_SPOKEN_LANGUAGE_BOOKING_STATUS_4(
        "fcsInterpreterSpokenLanguageBookingStatus4", new TypeReference<InterpreterBookingStatus>(){}),
    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_1(
        "fcsInterpreterSignLanguageBookingStatus1", new TypeReference<InterpreterBookingStatus>(){}),
    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_2(
        "fcsInterpreterSignLanguageBookingStatus2", new TypeReference<InterpreterBookingStatus>(){}),
    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_3(
        "fcsInterpreterSignLanguageBookingStatus3", new TypeReference<InterpreterBookingStatus>(){}),
    FCS_INTERPRETER_SIGN_LANGUAGE_BOOKING_STATUS_4(
        "fcsInterpreterSignLanguageBookingStatus4", new TypeReference<InterpreterBookingStatus>(){}),
    SEND_DIRECTION_DESCRIPTION(
        "sendDirectionDescription", new TypeReference<String>(){}),
    SEND_DIRECTION_LIST(
        "sendDirectionList", new TypeReference<String>(){}),
    DATE_OF_COMPLIANCE(
        "dateOfCompliance", new TypeReference<String>(){}),
    CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER(
        "currentCaseStateVisibleToAdminOfficer", new TypeReference<String>(){}),
    INTERPRETER_DETAILS(
        "interpreterDetails", new TypeReference<List<IdValue<InterpreterDetails>>>() {}),
    LOCAL_AUTHORITY_POLICY(
        "localAuthorityPolicy", new TypeReference<OrganisationPolicy>() {}),
    HEARING_CENTRE(
        "hearingCentre", new TypeReference<HearingCentre>(){}),
    LISTING_EVENT(
        "listingEvent", new TypeReference<String>(){}),
    LISTING_HEARING_DATE(
        "listingHearingDate", new TypeReference<String>(){}),
    LISTING_HEARING_LENGTH(
        "listingHearingLength", new TypeReference<String>() {}),
    LISTING_LOCATION(
        "listingLocation", new TypeReference<String>() {});

    private final String value;
    private final TypeReference typeReference;

    BailCaseFieldDefinition(String value, TypeReference typeReference) {
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
