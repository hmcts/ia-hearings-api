package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.ServiceHearingValuesProvider.HEARING_WINDOW_INTERVAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.ServiceHearingValuesProvider.HMCTS_SERVICE_ID;

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
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Region;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceHearingValuesProviderTest {

    private final String hmctsCaseNameInternal = "Eke Uke";
    private final String listCaseHearingLength = "120";
    private final String caseReference = "1234567891234567";
    private final String homeOfficeRef = "homeOfficeRef";
    private final String deepCaseLink = String.format("cases/case-details/%s#Overview", caseReference);
    private final String dateStr = "2023-08-01";
    private ServiceHearingValuesProvider serviceHearingValuesProvider;
    @Mock
    private DateProvider hearingServiceDateProvider;
    @Mock
    private AsylumCase asylumCase;

    @BeforeEach
    void setup() {

        when(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)).thenReturn(Optional.of(hmctsCaseNameInternal));
        when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.of(listCaseHearingLength));
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

        serviceHearingValuesProvider =
            new ServiceHearingValuesProvider(hearingServiceDateProvider);
    }

    @Test
    void should_get_service_hearing_values() {

        ServiceHearingValuesModel serviceHearingValuesModel = serviceHearingValuesProvider
            .provideServiceHearingValues(asylumCase, caseReference);

        assertThat(serviceHearingValuesModel).usingRecursiveComparison().isEqualTo(buildTestValues());
    }

    @Test
    public void should_throw_exception_when_hmcts_internal_case_name_is_missing() {

        when(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceHearingValuesProvider
            .provideServiceHearingValues(asylumCase, caseReference))
            .hasMessage("HMCTS internal case name is a required field")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    public void should_throw_exception_when_list_case_hearing_length_is_missing() {

        when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceHearingValuesProvider
            .provideServiceHearingValues(asylumCase, caseReference))
            .hasMessage("List case hearing length is a required field")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    private ServiceHearingValuesModel buildTestValues() {
        String hearingChannel = "INTER";
        String dateRangeEnd = "2023-08-15";
        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(HMCTS_SERVICE_ID)
            .hmctsInternalCaseName(hmctsCaseNameInternal)
            .publicCaseName("publicCaseName")
            .caseCategories(List.of(new CaseCategoryModel()))
            .caseAdditionalSecurityFlag(false)
            .caseDeepLink(deepCaseLink)
            .caserestrictedFlag(false)
            .externalCaseReference(homeOfficeRef)
            .caseManagementLocationCode(BaseLocation.BIRMINGHAM.getId())
            .caseSlaStartDate(dateStr)
            .autoListFlag(true)
            .hearingType("hearingType")
            .hearingWindow(HearingWindowModel.builder()
                .dateRangeStart(dateStr)
                .dateRangeEnd(dateRangeEnd)
                .build())
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingPriorityType(PriorityType.STANDARD)
            .numberOfPhysicalAttendees(0)
            .hearingInWelshFlag(false)
            .hearingLocations(HearingLocationModel.builder().build())
            .facilitiesRequired(Collections.emptyList())
            .listingComments("")
            .hearingRequester("")
            .privateHearingRequiredFlag(false)
            .caseInterpreterRequiredFlag(false)
            .panelRequirements(PanelRequirementsModel.builder().build())
            .leadJudgeContractType("")
            .judiciary(JudiciaryModel.builder().roleType(Collections.emptyList())
               .authorisationTypes(Collections.emptyList())
               .authorisationSubType(Collections.emptyList())
               .judiciaryPreferences(Collections.emptyList())
               .judiciarySpecialisms(Collections.emptyList())
               .panelComposition(Collections.emptyList())
               .build())
            .hearingIsLinkedFlag(false)
            .parties(Collections.emptyList())
            .caseflags(Caseflags.builder().build())
            .screenFlow(Collections.emptyList())
            .vocabulary(Collections.emptyList())
            .hearingChannels(List.of(hearingChannel))
            .build();
    }
}
