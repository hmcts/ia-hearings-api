package uk.gov.hmcts.reform.iahearingsapi.domain.entities.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.mappers.CaseDataToServiceHearingValuesMapper
    .HEARING_WINDOW_INTERVAL;

import java.time.LocalDate;
import java.time.ZonedDateTime;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Region;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseDataToServiceHearingValuesMapperTest {

    private final String homeOfficeRef = "homeOfficeRef";
    private final String dateStr = "2023-08-01";
    private CaseDataToServiceHearingValuesMapper mapper;
    @Mock
    private DateProvider hearingServiceDateProvider;
    @Mock
    private AsylumCase asylumCase;

    @BeforeEach
    void setup() {

        when(hearingServiceDateProvider.now()).thenReturn(LocalDate.parse(dateStr));
        String startDate = "2023-08-01T10:46:48.962301+01:00[Europe/London]";
        ZonedDateTime zonedDateTimeFrom = ZonedDateTime.parse(startDate);
        when(hearingServiceDateProvider.zonedNowWithTime()).thenReturn(zonedDateTimeFrom);
        String endDate = "2023-08-15T10:46:48.962301+01:00[Europe/London]";
        when(hearingServiceDateProvider
                 .calculateDueDate(zonedDateTimeFrom, HEARING_WINDOW_INTERVAL)).thenReturn(ZonedDateTime.parse(endDate));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRef));

        CaseManagementLocation caseManagementLocation = CaseManagementLocation
            .builder().region(Region.NATIONAL).baseLocation(BaseLocation.BIRMINGHAM).build();
        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.of(caseManagementLocation));

        DynamicList hearingChannel = new DynamicList("INTER");
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannel));

        mapper =
            new CaseDataToServiceHearingValuesMapper(hearingServiceDateProvider);
    }

    @Test
    void getCaseManagementLocationCode_should_return_location_code() {

        assertEquals(mapper.getCaseManagementLocationCode(asylumCase), BaseLocation.BIRMINGHAM.getId());
    }

    @Test
    void getCaseManagementLocationCode_should_return_null() {

        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.empty());

        assertNull(mapper.getCaseManagementLocationCode(asylumCase));
    }

    @Test
    void getHearingChannels_should_return_list_of_hearingChannels() {

        assertEquals(mapper.getHearingChannels(asylumCase), List.of("INTER"));
    }

    @Test
    void getHearingChannels_should_return_empty_list() {

        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        assertEquals(mapper.getHearingChannels(asylumCase), Collections.emptyList());
    }

    @Test
    void getExternalCaseReference_should_return_home_office_reference() {

        assertEquals(mapper.getExternalCaseReference(asylumCase), homeOfficeRef);
    }

    @Test
    void getExternalCaseReference_should_return_gwf_reference() {

        String gwfReference = "gwfReference";

        when(asylumCase.read(GWF_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(gwfReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertEquals(mapper.getExternalCaseReference(asylumCase), gwfReference);
    }

    @Test
    void getExternalCaseReference_should_return_null() {

        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertNull(mapper.getExternalCaseReference(asylumCase));
    }

    @Test
    void getHearingWindowModel_should_return_correct_date_range() {
        HearingWindowModel hearingWindowModel = mapper.getHearingWindowModel();

        assertEquals(hearingWindowModel.getDateRangeStart(), dateStr);
        assertEquals(hearingWindowModel.getDateRangeEnd(), "2023-08-15");
    }

    @Test
    void getCaseSlaStartDate_should_return_valid_date() {

        assertEquals(mapper.getCaseSlaStartDate(), dateStr);
    }

    @Test
    void getCaseDeepLink_should_return_valid_case_link() {

        String caseDeepLink = mapper.getCaseDeepLink("1234567891234567");
        assertEquals(caseDeepLink, "/cases/case-details/1234567891234567#Overview");
    }

}
