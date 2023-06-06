package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LocationType {

    COURT("court"),
    CLUSTER("cluster"),
    REGION("region");

    @JsonValue
    private final String locationLabel;
}
