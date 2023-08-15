package uk.gov.hmcts.reform.iahearingsapi.domain.service.holidaydates;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CountryHolidayDatesTest {

    private final LocalDate date = LocalDate.now();
    private final List<HolidayDate> events = List.of(new HolidayDate(date));
    private final CountryHolidayDates countryHolidayDates = new CountryHolidayDates(events);

    @Test
    void should_hold_unto_value() {
        assertEquals(events, countryHolidayDates.getEvents());
    }
}
