package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HearingChannel {

    INTER("In Person"),
    TEL("Telephone"),
    VID("Video"),
    NA("Not in Attendance"),
    ONPPRS("On the Papers");

    private final String label;
}
