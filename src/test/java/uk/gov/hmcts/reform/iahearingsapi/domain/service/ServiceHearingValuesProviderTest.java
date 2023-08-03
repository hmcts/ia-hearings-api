package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.ANONYMITY;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyFlagsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceHearingValuesProviderTest {

    private final String hmctsCaseNameInternal = "Eke Uke";
    private final String listCaseHearingLength = "120";
    private final String caseReference = "1234567891234567";
    private final String homeOfficeRef = "homeOfficeRef";
    private final String dateStr = "2023-08-01";
    private final List<String> hearingChannels = List.of("INTER");
    private final String dateRangeEnd = "2023-08-15";
    private final String caseDeepLink = "/cases/case-details/1234567891234567#Overview";
    private final String listingComments = "Customer behaviour: unfriendly";
    private final HearingWindowModel hearingWindowModel = HearingWindowModel.builder()
        .dateRangeStart(dateStr)
        .dateRangeEnd(dateRangeEnd)
        .build();
    private final Caseflags caseflags = Caseflags.builder()
        .flags(List.of(
            PartyFlagsModel.builder()
                .partyId("partyId")
                .flagId("id1")
                .flagDescription(ANONYMITY.getName())
                .partyName("")
                .flagStatus("Active")
                .build())).build();
    private ServiceHearingValuesProvider serviceHearingValuesProvider;
    @Mock
    private DateProvider hearingServiceDateProvider;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataToServiceHearingValuesMapper;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsToServiceHearingValuesMapper;

    @BeforeEach
    void setup() {

        when(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)).thenReturn(Optional.of(hmctsCaseNameInternal));
        when(asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class)).thenReturn(Optional.of(listCaseHearingLength));
        when(hearingServiceDateProvider.now()).thenReturn(LocalDate.parse(dateStr));
        String startDate = "2023-08-01T10:46:48.962301+01:00[Europe/London]";
        ZonedDateTime zonedDateTimeFrom = ZonedDateTime.parse(startDate);
        when(hearingServiceDateProvider.zonedNowWithTime()).thenReturn(zonedDateTimeFrom);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRef));

        CaseManagementLocation caseManagementLocation = CaseManagementLocation
            .builder().region(Region.NATIONAL).baseLocation(BaseLocation.BIRMINGHAM).build();
        when(asylumCase.read(CASE_MANAGEMENT_LOCATION, CaseManagementLocation.class))
            .thenReturn(Optional.of(caseManagementLocation));

        DynamicList hearingChannel = new DynamicList("INTER");
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannel));

        when(caseDataToServiceHearingValuesMapper.getHearingChannels(asylumCase)).thenReturn(hearingChannels);
        when(caseDataToServiceHearingValuesMapper.getExternalCaseReference(asylumCase)).thenReturn(homeOfficeRef);
        when(caseDataToServiceHearingValuesMapper.getHearingWindowModel()).thenReturn(hearingWindowModel);
        when(caseDataToServiceHearingValuesMapper.getCaseManagementLocationCode(asylumCase))
            .thenReturn(BaseLocation.BIRMINGHAM.getId());
        when(caseDataToServiceHearingValuesMapper.getCaseSlaStartDate()).thenReturn(dateStr);
        when(caseDataToServiceHearingValuesMapper.getCaseDeepLink(caseReference)).thenReturn(caseDeepLink);
        when(caseFlagsToServiceHearingValuesMapper.getPublicCaseName(asylumCase, caseReference))
            .thenReturn(caseReference);
        when(caseFlagsToServiceHearingValuesMapper.getCaseAdditionalSecurityFlag(asylumCase)).thenReturn(true);
        when(caseFlagsToServiceHearingValuesMapper.getAutoListFlag(asylumCase)).thenReturn(false);
        when(caseFlagsToServiceHearingValuesMapper.getHearingPriorityType(asylumCase))
            .thenReturn(PriorityType.STANDARD);
        when(caseFlagsToServiceHearingValuesMapper.getListingComments(asylumCase)).thenReturn(listingComments);
        when(caseFlagsToServiceHearingValuesMapper.getPrivateHearingRequiredFlag(asylumCase)).thenReturn(true);
        when(caseFlagsToServiceHearingValuesMapper.getCaseInterpreterRequiredFlag(asylumCase)).thenReturn(true);
        when(caseFlagsToServiceHearingValuesMapper.getCaseFlags(asylumCase)).thenReturn(caseflags);

        serviceHearingValuesProvider = new ServiceHearingValuesProvider(
            caseDataToServiceHearingValuesMapper,
            caseFlagsToServiceHearingValuesMapper
        );
    }

    @Test
    void should_get_service_hearing_values() {

        ServiceHearingValuesModel expected = buildTestValues();
        ServiceHearingValuesModel actual = serviceHearingValuesProvider
            .provideServiceHearingValues(asylumCase, caseReference);

        assertEquals(expected.getHmctsServiceId(), actual.getHmctsServiceId());
        assertEquals(expected.getHmctsInternalCaseName(), actual.getHmctsInternalCaseName());
        assertEquals(expected.getPublicCaseName(), actual.getPublicCaseName());
        assertEquals(expected.getCaseCategories(), actual.getCaseCategories());
        assertFalse(actual.isCaserestrictedFlag());
        assertEquals(expected.getCaseDeepLink(), actual.getCaseDeepLink());
        assertFalse(actual.isCaserestrictedFlag());
        assertEquals(expected.getExternalCaseReference(), actual.getExternalCaseReference());
        assertEquals(expected.getCaseManagementLocationCode(), actual.getCaseManagementLocationCode());
        assertEquals(expected.getCaseSlaStartDate(), actual.getCaseSlaStartDate());
        assertFalse(actual.isAutoListFlag());
        assertEquals(expected.getHearingType(), actual.getHearingType());
        assertEquals(expected.getHearingWindow(), actual.getHearingWindow());
        assertEquals(expected.getDuration(), actual.getDuration());
        assertEquals(expected.getHearingPriorityType(), actual.getHearingPriorityType());
        assertEquals(expected.getNumberOfPhysicalAttendees(), actual.getNumberOfPhysicalAttendees());
        assertFalse(actual.isHearingInWelshFlag());
        assertEquals(expected.getHearingLocations(), actual.getHearingLocations());
        assertEquals(expected.getFacilitiesRequired(), actual.getFacilitiesRequired());
        assertEquals(expected.getListingComments(), actual.getListingComments());
        assertEquals(expected.getHearingRequester(), actual.getHearingRequester());
        assertTrue(actual.isPrivateHearingRequiredFlag());
        assertTrue(actual.isCaseInterpreterRequiredFlag());
        assertEquals(expected.getPanelRequirements(), actual.getPanelRequirements());
        assertEquals(expected.getLeadJudgeContractType(), actual.getLeadJudgeContractType());
        assertEquals(expected.getJudiciary(), actual.getJudiciary());
        assertFalse(actual.isHearingIsLinkedFlag());
        assertEquals(expected.getParties(), actual.getParties());
        assertEquals(expected.getCaseflags(), actual.getCaseflags());
        assertEquals(expected.getScreenFlow(), actual.getScreenFlow());
        assertEquals(expected.getVocabulary(), actual.getVocabulary());
        assertEquals(expected.getHearingChannels(), actual.getHearingChannels());
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

        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(HMCTS_SERVICE_ID)
            .hmctsInternalCaseName(hmctsCaseNameInternal)
            .publicCaseName(caseReference)
            .caseCategories(List.of(new CaseCategoryModel()))
            .caseAdditionalSecurityFlag(true)
            .caseDeepLink(caseDeepLink)
            .caserestrictedFlag(false)
            .externalCaseReference(homeOfficeRef)
            .caseManagementLocationCode(BaseLocation.BIRMINGHAM.getId())
            .caseSlaStartDate(dateStr)
            .autoListFlag(false)
            .hearingType("hearingType")
            .hearingWindow(hearingWindowModel)
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingPriorityType(PriorityType.STANDARD)
            .numberOfPhysicalAttendees(0)
            .hearingInWelshFlag(false)
            .hearingLocations(HearingLocationModel.builder().build())
            .facilitiesRequired(Collections.emptyList())
            .listingComments(listingComments)
            .hearingRequester("")
            .privateHearingRequiredFlag(true)
            .caseInterpreterRequiredFlag(true)
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
            .caseflags(caseflags)
            .screenFlow(Collections.emptyList())
            .vocabulary(Collections.emptyList())
            .hearingChannels(hearingChannels)
            .build();
    }
}
