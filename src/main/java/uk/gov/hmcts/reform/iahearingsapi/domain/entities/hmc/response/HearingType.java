package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

@Getter
public enum HearingType {

    SUBSTANTIVE("substantive"),
    CMR("cmr");

    @JsonValue
    private final String label;

    HearingType(String label) {
        this.label = label;
    }

    public static Optional<HearingType> from(String value) {
        return stream(values())
                .filter(v -> Objects.equals(v.label, value))
                .findFirst();
    }
}
