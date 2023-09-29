package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingType {

    CASE_MANAGEMENT_REVIEW("BFA1-CMR"),
    COSTS("BFA1-COS"),
    BAIL("BFA1-BAI"),
    SUBSTANTIVE("BFA1-SUB");

    private final String key;
}
