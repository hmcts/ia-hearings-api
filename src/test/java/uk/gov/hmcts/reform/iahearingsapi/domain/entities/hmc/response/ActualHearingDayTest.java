package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ActualHearingDayTest {

    private static final String HEARING_DATE = "2023-01-20";

    @Test
    void days_with_same_hearing_date_are_equal_regardless_of_other_fields() {
        ActualHearingDay day = ActualHearingDay.builder()
            .hearingDate(HEARING_DATE)
            .hearingStartTime("10:00")
            .hearingEndTime("12:00")
            .pausedDateTimes(List.of(new PausedDateTime()))
            .actualDayParties(List.of(new ActualDayParty()))
            .notRequired("No")
            .build();
        ActualHearingDay otherDay = ActualHearingDay.builder()
            .hearingDate(HEARING_DATE)
            .hearingStartTime("14:00")
            .hearingEndTime("16:00")
            .notRequired("Yes")
            .build();

        assertEquals(day, otherDay);
        assertEquals(day.hashCode(), otherDay.hashCode());
    }

    @Test
    void days_with_different_hearing_dates_are_not_equal() {
        ActualHearingDay day = ActualHearingDay.builder().hearingDate(HEARING_DATE).build();
        ActualHearingDay otherDay = ActualHearingDay.builder().hearingDate("2023-01-21").build();

        assertNotEquals(day, otherDay);
    }

    @Test
    void days_with_null_hearing_dates_are_equal() {
        assertEquals(new ActualHearingDay(), ActualHearingDay.builder().hearingStartTime("10:00").build());
    }

    @Test
    void set_keeps_one_day_per_hearing_date() {
        Set<ActualHearingDay> days = new HashSet<>();
        days.add(ActualHearingDay.builder().hearingDate(HEARING_DATE).hearingStartTime("10:00").build());
        days.add(ActualHearingDay.builder().hearingDate(HEARING_DATE).hearingStartTime("14:00").build());
        days.add(ActualHearingDay.builder().hearingDate("2023-01-21").build());

        assertEquals(2, days.size());
    }
}
