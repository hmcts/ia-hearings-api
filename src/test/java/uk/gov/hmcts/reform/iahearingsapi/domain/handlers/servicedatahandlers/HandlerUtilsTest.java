package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HandlerUtilsTest {
    private static final String GLASGOW_EPIMMS_ID = "366559";
    private static final String BIRMINGHAM_EPIMMS_ID = "231596";

    @Test
    void getHearingDateAndTime() {
        LocalDateTime currentHearingDateTime = LocalDateTime.of(2023, 10, 6, 0, 0);
        LocalDateTime expectedGlasgowHearingDateTime = currentHearingDateTime.with(LocalTime.of(9, 45));
        LocalDateTime expectedNonGlasgowHearingDateTime = currentHearingDateTime.with(LocalTime.of(10, 0));

        assertEquals(expectedGlasgowHearingDateTime,
            HandlerUtils.getHearingDateAndTime(currentHearingDateTime, GLASGOW_EPIMMS_ID));

        assertEquals(expectedNonGlasgowHearingDateTime,
            HandlerUtils.getHearingDateAndTime(currentHearingDateTime, "other_hearing_centre_id"));
    }

    @Test
    void getLocation_should_return_remote_hearing_for_remote_only_channels() {
        assertEquals(HearingCentre.REMOTE_HEARING,
            HandlerUtils.getLocation(List.of(HearingChannel.VID), BIRMINGHAM_EPIMMS_ID));
        assertEquals(HearingCentre.REMOTE_HEARING,
            HandlerUtils.getLocation(List.of(HearingChannel.TEL), BIRMINGHAM_EPIMMS_ID));
    }

    @Test
    void getLocation_should_return_venue_centre_for_in_person_channel() {
        assertEquals(HearingCentre.BIRMINGHAM,
            HandlerUtils.getLocation(List.of(HearingChannel.INTER), BIRMINGHAM_EPIMMS_ID));
        assertEquals(HearingCentre.BIRMINGHAM,
            HandlerUtils.getLocation(List.of(HearingChannel.INTER, HearingChannel.VID), BIRMINGHAM_EPIMMS_ID));
    }

    @Test
    void getCmrLocation_should_return_venue_centre_regardless_of_channels() {
        assertEquals(HearingCentre.BIRMINGHAM,
            HandlerUtils.getCmrLocation(List.of(HearingChannel.INTER), BIRMINGHAM_EPIMMS_ID));
        assertEquals(HearingCentre.BIRMINGHAM,
            HandlerUtils.getCmrLocation(List.of(HearingChannel.VID), BIRMINGHAM_EPIMMS_ID));
        assertEquals(HearingCentre.BIRMINGHAM,
            HandlerUtils.getCmrLocation(List.of(HearingChannel.TEL), BIRMINGHAM_EPIMMS_ID));
    }
}