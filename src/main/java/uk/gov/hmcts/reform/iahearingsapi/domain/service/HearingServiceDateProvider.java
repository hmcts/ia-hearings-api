package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.holidaydates.HolidayService;

@Component
@RequiredArgsConstructor
public class HearingServiceDateProvider implements DateProvider {

    private final HolidayService holidayService;

    public LocalDate now() {
        return LocalDate.now();
    }

    public LocalDateTime nowWithTime() {
        return LocalDateTime.now();
    }

    public ZonedDateTime zonedNowWithTime() {
        return ZonedDateTime.now();
    }

    public ZonedDateTime calculateDelayUntil(ZonedDateTime eventDateTime, int delayDuration) {
        if (delayDuration <= 0) {
            return eventDateTime;
        }

        final ZonedDateTime zonedDateTime = addWorkingDaysForDelayDuration(eventDateTime, delayDuration);

        return resetTo4PmTime(zonedDateTime);
    }

    public ZonedDateTime calculateDueDate(ZonedDateTime delayUntil, int workingDaysAllowed) {
        final ZonedDateTime zonedDateTime = addWorkingDays(delayUntil, workingDaysAllowed);

        return resetTo4PmTime(zonedDateTime);
    }

    private ZonedDateTime addWorkingDays(ZonedDateTime dueDate, int numberOfDays) {
        if (numberOfDays == 0) {
            return dueDate;
        }

        ZonedDateTime newDate = dueDate.plusDays(1);
        if (holidayService.isWeekend(newDate) || holidayService.isHoliday(newDate)) {
            return addWorkingDays(newDate, numberOfDays);
        } else {
            return addWorkingDays(newDate, numberOfDays - 1);
        }
    }

    private ZonedDateTime addWorkingDaysForDelayDuration(ZonedDateTime eventDate, int delayDuration) {

        ZonedDateTime newDate = eventDate.plusDays(delayDuration);

        if (holidayService.isWeekend(newDate) || holidayService.isHoliday(newDate)) {
            return addWorkingDaysForDelayDuration(eventDate, delayDuration + 1);
        }

        return newDate;
    }

    private ZonedDateTime resetTo4PmTime(ZonedDateTime eventDateTime) {
        final LocalTime fourPmTime = LocalTime.of(16, 0, 0, 0);

        return ZonedDateTime.of(eventDateTime.toLocalDate(), fourPmTime, eventDateTime.getZone());
    }
}
