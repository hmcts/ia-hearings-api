package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import java.time.LocalDateTime;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HandlerUtilsTest {
    private static final String GLASGOW_EPIMMS_ID = "366559";

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
}