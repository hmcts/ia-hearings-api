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
    APPELLANT_LEVEL_FLAGS("appellantLevelFlags", new TypeReference<StrategicCaseFlag>() {
    }),
    WITNESS_LEVEL_FLAGS(
        "witnessLevelFlags", new TypeReference<List<IdValue<StrategicCaseFlag>>>() {}),
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
        "sponsorMobileNumber", new TypeReference<String>(){});



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
