package uk.gov.hmcts.reform.iahearingsapi.domain.service.holidaydates;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class UkHolidayDatesTest {

    private final List<HolidayDate> events = List.of(new HolidayDate(LocalDate.now()));
    private final CountryHolidayDates countryHolidayDates = new CountryHolidayDates(events);
    private final UkHolidayDates ukHolidayDates = new UkHolidayDates(countryHolidayDates);

    @Test
    void should_hold_unto_value() {
        assertEquals(countryHolidayDates, ukHolidayDates.getEnglandAndWales());

    }
}
