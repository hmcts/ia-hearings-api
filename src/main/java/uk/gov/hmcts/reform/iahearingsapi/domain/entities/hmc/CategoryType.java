package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CategoryType {

    CASE_TYPE("caseType"),
    CASE_SUB_TYPE("caseSubType");

    @JsonValue
    private final String categoryType;

    CategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getCategoryType() {
        return categoryType;
    }
}
