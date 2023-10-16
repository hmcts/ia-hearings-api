package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.stream;

@Getter
@RequiredArgsConstructor
public enum HearingChannel {

    INTER("In Person"),
    TEL("Telephone"),
    VID("Video"),
    NA("Not in Attendance"),
    ONPPRS("On the Papers");

    private final String label;

    public static Optional<HearingChannel> from(
        String value
    ) {
        return stream(values())
            .filter(v -> Objects.equals(v.label, value))
            .findFirst();
    }
}
