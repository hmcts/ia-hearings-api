package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HearingsUtilsTest {

    @Test
    void testConvertToLocalStringFormat() {
        LocalDateTime dateTime = LocalDateTime.of(2023, 10, 6, 12, 0);

        String formattedDate = HearingsUtils.convertToLocalStringFormat(dateTime);

        assertEquals("06 October 2023", formattedDate);
    }

    @Test
    void testConvertToLocalDateFormat() {
        String dateStr = "2023-10-06";

        LocalDateTime localDateTime = HearingsUtils.convertToLocalDateFormat(dateStr);

        assertEquals(LocalDateTime.of(2023, 10, 6, 0, 0), localDateTime);
    }

    @Test
    void testConvertToLocalDateTimeFormat() {
        String dateTimeStr = "2023-10-06T12:00:00.000";

        LocalDateTime localDateTime = HearingsUtils.convertToLocalDateTimeFormat(dateTimeStr);

        assertEquals(LocalDateTime.of(2023, 10, 6, 12, 0, 0), localDateTime);
    }
}
