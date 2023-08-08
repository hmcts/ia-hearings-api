package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;

public enum AsylumCaseFieldDefinition {

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
        "listCaseHearingLength", new TypeReference<String>() {});



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
