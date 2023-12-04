package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.BailStrategicCaseFlag;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

import java.util.Arrays;
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
        "fcsLevelFlags", new TypeReference<List<IdValue<BailStrategicCaseFlag>>>() {}),
    DISABILITY_YESNO(
        "applicantDisability1", new TypeReference<YesOrNo>(){}),
    APPLICANT_DISABILITY_DETAILS(
        "applicantDisabilityDetails", new TypeReference<String>(){}),;


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
