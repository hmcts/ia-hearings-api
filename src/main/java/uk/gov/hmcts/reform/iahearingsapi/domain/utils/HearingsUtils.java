package uk.gov.hmcts.reform.iahearingsapi.domain.utils;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HEARING_CENTRE_REF_DATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

public final class HearingsUtils {

    private HearingsUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DD_MMMM_YYYY = "dd MMMM yyyy";
    public static final String YYYY_MM_DD_HH_mm_ss_sss = "yyyy-MM-dd'T'HH:mm:ss.SSS";

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

    public static String getEpimsId(BailCase bailCase) {
        return bailCase.read(HEARING_CENTRE_REF_DATA, DynamicList.class)
            .map(dynamicList -> dynamicList.getValue().getCode())
            .orElse("");
    }

    public static boolean isAppealsLocationRefDataEnabled(AsylumCase asylumCase) {

        return asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)
            .map(yesOrNo -> yesOrNo.equals(YES))
            .orElse(false);
    }

    public static boolean isDecisionWithoutHearingAppeal(AsylumCase asylumCase) {
        if (HearingsUtils.isAppealsLocationRefDataEnabled(asylumCase)) {
            return asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class)
                .map(yesOrNo -> YES == yesOrNo).orElse(false);
        }
        return asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(hearingCentre -> DECISION_WITHOUT_HEARING == hearingCentre).orElse(false);
    }
}
