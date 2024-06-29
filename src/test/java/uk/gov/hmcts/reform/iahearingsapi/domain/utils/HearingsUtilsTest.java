package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class HearingsUtilsTest {

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
        String dateStr = "2023-10-06";

        LocalDateTime localDateTime = HearingsUtils.convertToLocalDateTimeFormat(dateStr);

        assertEquals(LocalDateTime.of(2023, 10, 6, 0, 0, 0), localDateTime);
    }
}
