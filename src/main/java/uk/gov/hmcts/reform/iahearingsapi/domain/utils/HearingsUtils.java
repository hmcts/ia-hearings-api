package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class HearingsUtils {

    private HearingsUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DD_MMMM_YYYY = "dd MMMM yyyy";

    public static String convertToLocalStringFormat(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DD_MMMM_YYYY);
        return dateTime.format(formatter);
    }

    public static LocalDateTime convertToLocalDateFormat(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        return LocalDateTime.of(LocalDate.parse(date, formatter), LocalDateTime.now().toLocalTime());
    }

    public static LocalDateTime convertToLocalDateTimeFormat(String date) {
        return LocalDateTime.parse(date);
    }
}
