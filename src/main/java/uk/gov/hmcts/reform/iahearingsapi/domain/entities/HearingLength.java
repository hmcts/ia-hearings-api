package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum HearingLength {

    LENGTH_30_MINUTES(30),
    LENGTH_1_HOUR(60),
    LENGTH_1_HOUR_30_MINUTES(90),
    LENGTH_2_HOURS(120),
    LENGTH_2_HOURS_30_MINUTES(150),
    LENGTH_3_HOURS(180),
    LENGTH_3_HOURS_30_MINUTES(210),
    LENGTH_4_HOURS(240),
    LENGTH_4_HOURS_30_MINUTES(270),
    LENGTH_5_HOURS(300),
    LENGTH_5_HOURS_30_MINUTES(330),
    LENGTH_6_HOURS(360);

    @JsonValue
    private final int value;

    HearingLength(int value) {
        this.value = value;
    }

    public static Optional<HearingLength> from(
        int value
    ) {
        return stream(values())
            .filter(v -> v.getValue() == value)
            .findFirst();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public String convertToHourMinuteString() {

        int hours = value / 60;
        int remainingMinutes = value % 60;

        if (hours > 0 && remainingMinutes > 0) {
            return hours + " hours " + remainingMinutes + " minutes";
        } else if (hours > 1) {
            return hours + " hours";
        } else if (hours == 1) {
            return hours + " hour";
        } else {
            return remainingMinutes + " minutes";
        }
    }
}
