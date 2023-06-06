package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum CaseCategoryType {

    CASE_TYPE("caseType"),
    CASE_SUBTYPE("caseSubType");

    @JsonValue
    private final String categoryLabel;
}
