package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.DocumentTag.BAIL_SUBMISSION;

@Service
@RequiredArgsConstructor
public class BailCaseDataToServiceHearingValuesMapper {
    static final int HEARING_START_WINDOW_INTERVAL_DEFAULT = 2;
    static final int HEARING_WINDOW_END_INTERVAL_DEFAULT = 7;

    private final DateProvider hearingServiceDateProvider;

    public String getExternalCaseReference(BailCase bailCase) {
        return bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(null);
    }

    public List<String> getHearingChannels(BailCase bailCase) {
        if (bailCase.read(VIDEO_HEARING1, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return List.of("Video");
        }
        return Collections.emptyList();
    }

    public String getListingComments(BailCase bailCase) {
        if (bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
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

    public String getCaseSlaStartDate(BailCase bailCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalNotificationLetters =
            bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA);
        return optionalNotificationLetters
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(d -> d.getTag() == BAIL_SUBMISSION)
            .findFirst().orElseThrow(() -> new RequiredFieldMissingException(
                BAIL_SUBMISSION + " document not available"))
            .getDateUploaded();
    }
}
