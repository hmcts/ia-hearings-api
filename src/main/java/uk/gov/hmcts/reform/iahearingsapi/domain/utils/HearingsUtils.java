package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class HearingsUtils {

    private HearingsUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DD_MMMM_YYYY = "dd MMMM yyyy";
    public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    public static String convertToLocalStringFormat(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMMM_YYYY);
        return dateTime.format(formatter);
    }

    public static LocalDateTime convertToLocalDateFormat(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate.atStartOfDay();
    }

    public static LocalDateTime convertToLocalDateTimeFormat(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return LocalDateTime.of(localDate, LocalTime.of(0, 0, 0));
    }

    public static LocalDateTime convertFromUTC(LocalDateTime utcDate) {
        ZonedDateTime utcZonedDateTime = utcDate.atZone(ZoneId.of("UTC"));
        ZonedDateTime ukZonedDateTime = utcZonedDateTime.withZoneSameInstant(ZoneId.of("Europe/London"));
        return ukZonedDateTime.toLocalDateTime();
    }
}
