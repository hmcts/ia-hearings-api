package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.ANONYMITY;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ResourceLoader;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Region;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyFlagsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.ListingCommentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;

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
    private final List<PartyDetailsModel> partyDetails = Arrays.asList(
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build()
    );
    private final CaseCategoryModel caseCategoryCaseType = new CaseCategoryModel();
    private final CaseCategoryModel caseCategoryCaseSubType = new CaseCategoryModel();
    private ServiceHearingValuesProvider serviceHearingValuesProvider;
    @Mock
    private DateProvider hearingServiceDateProvider;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;
    @Mock
    private PartyDetailsMapper partyDetailsMapper;
    @Mock
    private ListingCommentsMapper listingCommentsMapper;

    @Mock
    private ResourceLoader resourceLoader;
    private final String baseUrl = "http://localhost:3002";
    private String caseCategoriesValue = "BFA1-TST";
    private final String serviceId = "BFA1";

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

        when(caseDataMapper.getHearingChannels(asylumCase)).thenReturn(hearingChannels);
        when(caseDataMapper.getExternalCaseReference(asylumCase)).thenReturn(homeOfficeRef);
        when(caseDataMapper.getHearingWindowModel()).thenReturn(hearingWindowModel);
        when(caseDataMapper.getCaseManagementLocationCode(asylumCase))
            .thenReturn(BaseLocation.BIRMINGHAM.getId());
        when(caseDataMapper.getCaseDeepLink(caseReference)).thenReturn(caseDeepLink);
        when(caseDataMapper.getCaseSlaStartDate()).thenReturn(dateStr);
        when(caseFlagsMapper.getPublicCaseName(asylumCase, caseReference))
            .thenReturn(caseReference);
        when(caseFlagsMapper.getCaseAdditionalSecurityFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getAutoListFlag(asylumCase)).thenReturn(false);
        when(caseFlagsMapper.getHearingPriorityType(asylumCase))
            .thenReturn(PriorityType.STANDARD);
        when(listingCommentsMapper.getListingComments(asylumCase, caseFlagsMapper, caseDataMapper))
            .thenReturn(listingComments);
        when(caseFlagsMapper.getPrivateHearingRequiredFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCaseInterpreterRequiredFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCaseFlags(asylumCase, caseReference)).thenReturn(caseflags);
        when(partyDetailsMapper.map(asylumCase, caseFlagsMapper, caseDataMapper)).thenReturn(partyDetails);

        caseCategoryCaseType.setCategoryType(CategoryType.CASE_TYPE);
        caseCategoryCaseType.setCategoryValue(caseCategoriesValue);
        caseCategoryCaseType.setCategoryParent("");

        caseCategoryCaseSubType.setCategoryType(CategoryType.CASE_SUB_TYPE);
        caseCategoryCaseSubType.setCategoryValue(caseCategoriesValue);
        caseCategoryCaseSubType.setCategoryParent(caseCategoriesValue);

        serviceHearingValuesProvider = new ServiceHearingValuesProvider(
            caseDataMapper,
            caseFlagsMapper,
            languageAndAdjustmentsMapper,
            partyDetailsMapper,
            listingCommentsMapper,
            resourceLoader
        );

        serviceHearingValuesProvider.setBaseUrl(baseUrl);
        serviceHearingValuesProvider.setCaseCategoriesValue(caseCategoriesValue);
        serviceHearingValuesProvider.setServiceId(serviceId);
    }

    @Test
    void should_get_service_hearing_values() throws JSONException {

        ServiceHearingValuesModel expected = buildTestValues();
        ServiceHearingValuesModel actual = serviceHearingValuesProvider
            .provideServiceHearingValues(asylumCase, caseReference);

        assertEquals(expected, actual);
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

    private ServiceHearingValuesModel buildTestValues() throws JSONException {

        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(serviceId)
            .hmctsInternalCaseName(hmctsCaseNameInternal)
            .publicCaseName(caseReference)
            .caseCategories(List.of(caseCategoryCaseType, caseCategoryCaseSubType))
            .caseAdditionalSecurityFlag(true)
            .caseDeepLink(baseUrl + caseDeepLink)
            .caserestrictedFlag(false)
            .externalCaseReference(homeOfficeRef)
            .caseManagementLocationCode(BaseLocation.BIRMINGHAM.getId())
            .caseSlaStartDate(dateStr)
            .autoListFlag(false)
            .hearingType(null)
            .hearingWindow(hearingWindowModel)
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingPriorityType(PriorityType.STANDARD)
            .numberOfPhysicalAttendees(0)
            .hearingInWelshFlag(false)
            .hearingLocations(Collections.emptyList())
            .numberOfPhysicalAttendees(0)
            .facilitiesRequired(Collections.emptyList())
            .listingComments(listingComments)
            .hearingRequester("")
            .privateHearingRequiredFlag(true)
            .caseInterpreterRequiredFlag(true)
            .panelRequirements(null)
            .leadJudgeContractType("")
            .judiciary(JudiciaryModel.builder().roleType(Collections.emptyList())
               .authorisationTypes(Collections.emptyList())
               .authorisationSubType(Collections.emptyList())
               .judiciaryPreferences(Collections.emptyList())
               .judiciarySpecialisms(Collections.emptyList())
               .panelComposition(Collections.emptyList())
               .build())
            .hearingIsLinkedFlag(false)
            .parties(partyDetails)
            .caseflags(caseflags)
            .screenFlow(serviceHearingValuesProvider.getScreenFlowJson())
            .vocabulary(Collections.emptyList())
            .hearingChannels(hearingChannels)
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .build();
    }

    @Test
    void should_find_number_of_physical_attendees() {
        partyDetails.get(0).setIndividualDetails(IndividualDetailsModel.builder()
                                                     .preferredHearingChannel("INTER").build());
        partyDetails.get(1).setIndividualDetails(IndividualDetailsModel.builder()
                                                     .preferredHearingChannel("INTER").build());

        int expectedPartiesInPerson = serviceHearingValuesProvider.getNumberOfPhysicalAttendees(partyDetails);

        assertEquals(expectedPartiesInPerson, 2);
    }
}
