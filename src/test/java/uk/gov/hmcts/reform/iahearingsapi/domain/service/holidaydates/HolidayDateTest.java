package uk.gov.hmcts.reform.iahearingsapi.domain.service.holidaydates;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class HolidayDateTest {

    private final LocalDate date = LocalDate.now();
    private final HolidayDate holidayDate = new HolidayDate(date);

    @Test
    void should_hold_unto_value() {
        assertEquals(date, holidayDate.getDate());
    }
}
