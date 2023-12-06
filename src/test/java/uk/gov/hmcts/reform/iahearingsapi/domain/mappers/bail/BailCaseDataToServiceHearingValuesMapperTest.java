package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DISABILITY_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.VIDEO_HEARING1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.DocumentTag.BAIL_SUBMISSION;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BailCaseDataToServiceHearingValuesMapperTest {

    private final String homeOfficeRef = "homeOfficeRef";
    private final String dateStr = "2023-08-01";
    private BailCaseDataToServiceHearingValuesMapper mapper;
    @Mock
    private DateProvider hearingServiceDateProvider;
    @Mock
    private BailCase bailCase;
    @Mock
    private Document document;

    private final IdValue<DocumentWithMetadata> bailSubmission = new IdValue<>(
        "1",
        new DocumentWithMetadata(
            document,
            "Some description",
            dateStr,
            BAIL_SUBMISSION,
            ""
        )
    );

    private static final List<String> hearingChannels = List.of("Video");

    @BeforeEach
    void setup() {
        when(hearingServiceDateProvider.now()).thenReturn(LocalDate.parse(dateStr));
        String startDate = "2023-08-01T10:46:48.962301+01:00[Europe/London]";
        ZonedDateTime zonedDateTimeFrom = ZonedDateTime.parse(startDate);
        when(hearingServiceDateProvider.zonedNowWithTime()).thenReturn(zonedDateTimeFrom);
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRef));

        mapper =
            new BailCaseDataToServiceHearingValuesMapper(hearingServiceDateProvider);
    }

    @Test
    void getExternalCaseReference_should_return_home_office_reference() {

        assertEquals(homeOfficeRef, mapper.getExternalCaseReference(bailCase));
    }

    @Test
    void getExternalCaseReference_should_return_null() {

        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertNull(mapper.getExternalCaseReference(bailCase));
    }

    @Test
    void getHearingWindowModel_should_return_correct_date_range() {
        ZonedDateTime expectedStartDate = ZonedDateTime.now().plusDays(2L);
        ZonedDateTime expectedEndDate = ZonedDateTime.now().plusDays(7L);
        when(hearingServiceDateProvider
                 .calculateDueDate(hearingServiceDateProvider.zonedNowWithTime(), 2))
            .thenReturn(expectedStartDate);

        when(hearingServiceDateProvider
                 .calculateDueDate(hearingServiceDateProvider.zonedNowWithTime(), 7))
            .thenReturn(expectedEndDate);
        HearingWindowModel hearingWindowModel = mapper.getHearingWindowModel();

        assertEquals(expectedStartDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                     hearingWindowModel.getDateRangeStart());
        assertEquals(expectedEndDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                     hearingWindowModel.getDateRangeEnd());
    }

    @Test
    void getHearingChannels_should_return_video_if_video_hearing_is_yes() {
        when(bailCase.read(VIDEO_HEARING1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        List<String> resultHearingChannels = mapper.getHearingChannels(bailCase);

        assertEquals(hearingChannels, resultHearingChannels);
    }

    @Test
    void getHearingChannels_should_return_empty_list_if_video_hearing_is_no() {
        when(bailCase.read(VIDEO_HEARING1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        List<String> resultHearingChannels = mapper.getHearingChannels(bailCase);

        assertEquals(Collections.emptyList(), resultHearingChannels);
    }

    @Test
    void getListingComments_should_return_disability_details_if_disability_is_yes() {
        when(bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(APPLICANT_DISABILITY_DETAILS, String.class))
            .thenReturn(Optional.of("Disability description"));

        String listingComments = mapper.getListingComments(bailCase);

        assertEquals("Disability description", listingComments);
    }

    @Test
    void getListingComments_should_return_empty_string_if_disability_is_yes_and_disability_details_is_null() {
        when(bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(APPLICANT_DISABILITY_DETAILS, String.class)).thenReturn(Optional.empty());

        String listingComments = mapper.getListingComments(bailCase);

        assertEquals("", listingComments);
    }

    @Test
    void getListingComments_should_return_empty_string_if_disability_is_no() {
        when(bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        String listingComments = mapper.getListingComments(bailCase);

        assertEquals("", listingComments);
    }

    @Test
    void getCaseSlaStartDate_should_return_valid_date() {
        List<IdValue<DocumentWithMetadata>> allDocuments = new ArrayList<>();
        allDocuments.add(bailSubmission);
        when(bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(allDocuments));
        assertEquals(dateStr, mapper.getCaseSlaStartDate(bailCase));
    }

    @Test
    void getCaseSlaStartDate_should_throw_exception_if_bail_submission_document_not_found() {
        when(bailCase.read(APPLICANT_DOCUMENTS_WITH_METADATA)).thenReturn(Optional.of(Collections.emptyList()));

        assertThatThrownBy(() -> mapper.getCaseSlaStartDate(bailCase))
            .hasMessage(BAIL_SUBMISSION + " document not available")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }
}
