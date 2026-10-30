package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

@Getter
public enum HearingResult {

    HEARD("heard"),
    PART_HEARD("partHeard"),
    WITHDRAWN("withdrawn");

    @JsonValue
    private final String label;

    HearingResult(String label) {
        this.label = label;
    }

    public static Optional<HearingResult> from(String value) {
        return stream(values())
                .filter(v -> Objects.equals(v.label, value))
                .findFirst();
    }
}
