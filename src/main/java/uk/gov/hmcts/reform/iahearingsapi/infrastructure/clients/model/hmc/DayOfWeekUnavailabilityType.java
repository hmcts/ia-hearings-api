package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DayOfWeekUnavailabilityType {

    AM("AM"),
    PM("PM"),
    ALL_DAY("All Day");

    @JsonValue
    private final String label;
}
