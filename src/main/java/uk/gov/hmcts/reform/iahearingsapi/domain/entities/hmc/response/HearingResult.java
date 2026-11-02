package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

@Getter
public enum HearingResult {

    HEARD("heard", "COMPLETED"),
    PART_HEARD("partHeard", "ADJOURNED"),
    WITHDRAWN("withdrawn", "CANCELLED");

    @JsonValue
    private final String label;

    @JsonValue
    private final String hmcValue;

    HearingResult(String label, String hmcValue) {
        this.label = label;
        this.hmcValue = hmcValue;
    }

    public static Optional<HearingResult> from(String value) {
        return stream(values())
                .filter(v -> Objects.equals(v.label, value))
                .findFirst();
    }
}
