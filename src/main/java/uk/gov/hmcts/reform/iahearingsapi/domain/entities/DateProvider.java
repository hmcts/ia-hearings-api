package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public interface DateProvider {

    LocalDate now();

    LocalDateTime nowWithTime();

    ZonedDateTime zonedNowWithTime();

    ZonedDateTime calculateDelayUntil(ZonedDateTime eventDateTime, int delayDuration);

    ZonedDateTime calculateDueDate(ZonedDateTime delayUntil, int workingDaysAllowed);
}
