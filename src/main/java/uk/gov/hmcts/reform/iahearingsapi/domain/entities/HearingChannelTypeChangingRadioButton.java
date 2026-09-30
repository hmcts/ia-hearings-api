package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

public enum HearingChannelTypeChangingRadioButton {

    INTER("In Person"),
    VID("Video"),
    TEL("Telephone"),
    ONPPRS("On the Papers"),
    NA("Not in Attendance");

    @JsonValue
    private final String value;

    HearingChannelTypeChangingRadioButton(String value) {
        this.value = value;
    }

    public static Optional<HearingChannelTypeChangingRadioButton> from(
        String value
    ) {
        return stream(values())
            .filter(v -> Objects.equals(v.value, value))
            .findFirst();
    }

    @Override
    public String toString() {
        return value;
    }
}
