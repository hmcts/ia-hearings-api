package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

@Getter
public enum HearingType {

    SUBSTANTIVE("substantive", "BFA1-SUB"),
    CMR("cmr", "BFA1-CMR");

    @JsonValue
    private final String label;

    @JsonValue
    private final String hmcValue;

    HearingType(String label, String hmcValue) {
        this.label = label;
        this.hmcValue = hmcValue;
    }

    public static Optional<HearingType> from(String value) {
        return stream(values())
                .filter(v -> Objects.equals(v.label, value))
                .findFirst();
    }
}
