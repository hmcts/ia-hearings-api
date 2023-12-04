package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.*;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.DISABILITY_YESNO;

@Service
@RequiredArgsConstructor
public class BailCaseDataToServiceHearingValuesMapper {
    static final int HEARING_START_WINDOW_INTERVAL_DEFAULT = 2;
    static final int HEARING_WINDOW_END_INTERVAL_DEFAULT = 7;

    private final DateProvider hearingServiceDateProvider;

    public String getCaseSlaStartDate(BailCase bailCase) {
        return null;
    }

    public String getListingComments(BailCase bailCase) {
        if (bailCase.read(DISABILITY_YESNO, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return bailCase.read(APPLICANT_DISABILITY_DETAILS, String.class).orElse("");
        }
        return "";
    }

    public HearingWindowModel getHearingWindowModel() {
        ZonedDateTime now = hearingServiceDateProvider.zonedNowWithTime();
        String dateRangeStart = hearingServiceDateProvider
            .calculateDueDate(now, HEARING_START_WINDOW_INTERVAL_DEFAULT)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dateRangeEnd = hearingServiceDateProvider
            .calculateDueDate(now, HEARING_WINDOW_END_INTERVAL_DEFAULT)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return HearingWindowModel.builder()
            .dateRangeStart(dateRangeStart)
            .dateRangeEnd(dateRangeEnd)
            .build();
    }
}
