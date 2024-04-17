package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DEPORTATION_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.S94B_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.HEARING_CENTRE_REF_DATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.Facilities.IAC_TYPE_C_CONFERENCE_EQUIPMENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.StrategicCaseFlagType.ANONYMITY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType.RP;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType.STANDARD;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ResourceLoader;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseManagementLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DateProvider;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Region;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.Caseflags;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.JudiciaryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyFlagsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ServiceHearingValuesModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.ListingCommentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.PayloadUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceHearingValuesProviderTest {

    private static final String LOCATION_OF_SCREEN_FLOW_FILE_TEST = "classpath:screenFlowTest.json";
    private static final String TRIBUNAL_JUDGE = "84";
    private static final String BAILS_LOCATION_REF_DATA_FEATURE = "bails-location-reference-data";
    private final String hmctsCaseNameInternal = "Eke Uke";
    private final String caseNameHmctsInternal = "John Doe";
    private final String listCaseHearingLength = "120";
    private final String caseReference = "1234567891234567";
    private final String homeOfficeRef = "homeOfficeRef";
    private final LocalDate date = LocalDate.of(2023, 8, 1);
    private final List<String> hearingChannels = List.of("INTER");
    private final List<String> bailHearingChannels = List.of("Video");
    private final String dateRangeEnd = "2023-08-15";
    private final String caseDeepLink = "/cases/case-details/1234567891234567#Overview";
    private final String listingComments = "Customer behaviour: unfriendly";
    private final String bailState = "applicationSubmitted";
    private final HearingWindowModel hearingWindowModel = HearingWindowModel.builder()
        .dateRangeStart(date.toString())
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
    private final List<PartyDetailsModel> partyDetailsModels = Arrays.asList(
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build()
    );
    private final CaseCategoryModel caseCategoryCaseType = new CaseCategoryModel();
    private final CaseCategoryModel caseCategoryCaseSubType = new CaseCategoryModel();
    private final CaseCategoryModel bailCaseCategoryCaseType = new CaseCategoryModel();
    private final CaseCategoryModel bailCaseCategoryCaseSubType = new CaseCategoryModel();
    private ServiceHearingValuesProvider serviceHearingValuesProvider;
    @Mock
    private DateProvider hearingServiceDateProvider;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private BailCase bailCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private BailCaseDataToServiceHearingValuesMapper bailCaseDataMapper;
    @Mock
    private BailCaseFlagsToServiceHearingValuesMapper bailCaseFlagsMapper;
    @Mock
    private PartyDetailsMapper partyDetailsMapper;
    @Mock
    private ListingCommentsMapper listingCommentsMapper;
    @Mock
    private ResourceLoader resourceLoader;
    @Mock
    private FeatureToggler featureToggler;
    private final MockedStatic<PayloadUtils> payloadUtils = mockStatic(PayloadUtils.class);
    private final String baseUrl = "http://localhost:3002";
    private String caseCategoriesValue = "BFA1-TST";
    private final String serviceId = "BFA1";
    private final String bailServiceId = "BFA1-BLS";
    private final String bailHearingType = "BFA1-BAI";

    @BeforeEach
    void setup() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)).thenReturn(Optional.of(hmctsCaseNameInternal));
        when(hearingServiceDateProvider.now()).thenReturn(date);
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
        when(caseDataMapper.getHearingWindowModel(State.LISTING)).thenReturn(hearingWindowModel);
        when(caseDataMapper.getHearingWindowModel(true)).thenReturn(hearingWindowModel);
        when(caseDataMapper.getCaseManagementLocationCode(asylumCase))
            .thenReturn(BaseLocation.BIRMINGHAM.getId());
        when(caseDataMapper.getCaseDeepLink(caseReference)).thenReturn(caseDeepLink);
        when(caseDataMapper.getCaseSlaStartDate()).thenReturn(date);
        when(caseDataMapper.getHearingDuration(asylumCase)).thenReturn(Integer.parseInt(listCaseHearingLength));
        when(caseFlagsMapper.getPublicCaseName(asylumCase, caseReference))
            .thenReturn(caseReference);
        when(caseFlagsMapper.getCaseAdditionalSecurityFlag(asylumCase)).thenReturn(true);
        when(caseDataMapper.getHearingLinkedFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getHearingPriorityType(asylumCase))
            .thenReturn(STANDARD);
        when(listingCommentsMapper.getListingComments(asylumCase, caseFlagsMapper, caseDataMapper))
            .thenReturn(listingComments);
        when(caseFlagsMapper.getPrivateHearingRequiredFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCaseInterpreterRequiredFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCaseFlags(asylumCase, caseReference)).thenReturn(caseflags);
        when(partyDetailsMapper.mapAsylumPartyDetails(asylumCase, caseFlagsMapper, caseDataMapper))
            .thenReturn(partyDetailsModels);

        when(asylumCase.read(DEPORTATION_ORDER_OPTIONS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(RP));

        caseCategoryCaseType.setCategoryType(CategoryType.CASE_TYPE);
        caseCategoryCaseType.setCategoryValue(CaseTypeValue.RPD.getValue());
        caseCategoryCaseType.setCategoryParent("");

        caseCategoryCaseSubType.setCategoryType(CategoryType.CASE_SUB_TYPE);
        caseCategoryCaseSubType.setCategoryValue(CaseTypeValue.RPD.getValue());
        caseCategoryCaseSubType.setCategoryParent(CaseTypeValue.RPD.getValue());

        payloadUtils.when(() -> PayloadUtils.getNumberOfPhysicalAttendees(partyDetailsModels))
            .thenReturn(0);
        payloadUtils.when(() -> PayloadUtils.getCaseCategoriesValue(asylumCase))
            .thenReturn(List.of(caseCategoryCaseType, caseCategoryCaseSubType));

        when(bailCaseDataMapper.getHearingChannels(bailCase)).thenReturn(bailHearingChannels);
        when(bailCaseDataMapper.getExternalCaseReference(bailCase)).thenReturn(homeOfficeRef);
        when(bailCaseDataMapper.getCaseSlaStartDate(bailCase)).thenReturn(date.toString());
        when(bailCaseDataMapper.getHearingWindowModel(bailState)).thenReturn(hearingWindowModel);
        when(bailCaseDataMapper.getListingComments(bailCase)).thenReturn(listingComments);
        when(bailCaseFlagsMapper.getPublicCaseName(bailCase, caseReference)).thenReturn(caseReference);
        when(bailCaseFlagsMapper.getHearingPriorityType(bailCase)).thenReturn(STANDARD);
        when(bailCaseFlagsMapper.getCaseFlags(bailCase, caseReference)).thenReturn(caseflags);
        when(bailCaseDataMapper.getCaseManagementLocationCode(bailCase))
            .thenReturn(HearingCentre.MANCHESTER.getEpimsId());

        bailCaseCategoryCaseType.setCategoryType(CategoryType.CASE_TYPE);
        bailCaseCategoryCaseType.setCategoryValue(bailServiceId);
        bailCaseCategoryCaseType.setCategoryParent("");

        bailCaseCategoryCaseSubType.setCategoryType(CategoryType.CASE_SUB_TYPE);
        bailCaseCategoryCaseSubType.setCategoryValue(bailServiceId);
        bailCaseCategoryCaseSubType.setCategoryParent(bailServiceId);

        when(partyDetailsMapper.mapBailPartyDetails(bailCase, bailCaseFlagsMapper, bailCaseDataMapper))
            .thenReturn(partyDetailsModels);

        when(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class)).thenReturn(Optional.of(caseNameHmctsInternal));
        when(bailCase.read(CURRENT_CASE_STATE_VISIBLE_TO_ADMIN_OFFICER, String.class))
            .thenReturn(Optional.of(bailState));

        when(partyDetailsMapper.mapBailPartyDetails(bailCase, bailCaseFlagsMapper, bailCaseDataMapper))
            .thenReturn(partyDetailsModels);

        serviceHearingValuesProvider = new ServiceHearingValuesProvider(
            caseDataMapper,
            bailCaseDataMapper,
            caseFlagsMapper,
            bailCaseFlagsMapper,
            partyDetailsMapper,
            listingCommentsMapper,
            resourceLoader,
            featureToggler
        );

        serviceHearingValuesProvider.setBaseUrl(baseUrl);
        serviceHearingValuesProvider.setCaseCategoriesValue(caseCategoriesValue);
        serviceHearingValuesProvider.setBailCaseCategoriesValue(bailServiceId);
        serviceHearingValuesProvider.setServiceId(serviceId);
    }

    @AfterEach
    void tearDown() {
        payloadUtils.close();
    }

    @Test
    void should_get_service_hearing_values() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(Long.parseLong(caseReference));
        when(caseDetails.getState()).thenReturn(State.LISTING);
        ServiceHearingValuesModel expected = buildTestAsylumServiceHearingValuesModel();
        ServiceHearingValuesModel actual = serviceHearingValuesProvider
            .provideAsylumServiceHearingValues(caseDetails);

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void should_get_bail_service_hearing_values(boolean bailsLocationRefDataServiceEnabled) {

        when(featureToggler.getValue(BAILS_LOCATION_REF_DATA_FEATURE, false))
            .thenReturn(bailsLocationRefDataServiceEnabled);

        ServiceHearingValuesModel expected = buildTestBailServiceHearingValuesModel();

        if (bailsLocationRefDataServiceEnabled) {
            final String glasgow = "366559";
            when(bailCase.read(HEARING_CENTRE_REF_DATA, DynamicList.class))
                .thenReturn(
                    Optional.of(
                        new DynamicList(
                            new Value(glasgow, glasgow),
                            List.of(new Value(glasgow, glasgow))
                        )
                    )
                );

            expected.setCaseManagementLocationCode(glasgow);
        }

        ServiceHearingValuesModel actual = serviceHearingValuesProvider
            .provideBailServiceHearingValues(bailCase, caseReference);

        assertEquals(expected, actual);
    }

    @Test
    void should_get_service_hearing_values_with_facilities_when_s94B_is_enabled() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(Long.parseLong(caseReference));
        when(caseDetails.getState()).thenReturn(State.LISTING);
        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        ServiceHearingValuesModel expected = buildTestAsylumServiceHearingValuesModel();
        expected.setFacilitiesRequired(List.of(IAC_TYPE_C_CONFERENCE_EQUIPMENT.toString()));
        ServiceHearingValuesModel actual = serviceHearingValuesProvider
            .provideAsylumServiceHearingValues(caseDetails);

        assertEquals(expected, actual);
    }

    @Test
    public void should_throw_exception_when_hmcts_internal_case_name_is_missing() {

        when(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceHearingValuesProvider
            .provideAsylumServiceHearingValues(caseDetails))
            .hasMessage("HMCTS internal case name is a required field")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    private ServiceHearingValuesModel buildTestAsylumServiceHearingValuesModel() {

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
            .caseSlaStartDate(date.toString())
            .autoListFlag(false)
            .hearingType(null)
            .hearingWindow(hearingWindowModel)
            .duration(Integer.parseInt(listCaseHearingLength))
            .hearingPriorityType(STANDARD)
            .numberOfPhysicalAttendees(0)
            .hearingInWelshFlag(false)
            .hearingLocations(Collections.emptyList())
            .facilitiesRequired(Collections.emptyList())
            .listingComments(listingComments)
            .hearingRequester("")
            .privateHearingRequiredFlag(true)
            .caseInterpreterRequiredFlag(true)
            .panelRequirements(PanelRequirementsModel.builder()
                                   .authorisationSubType(Collections.emptyList())
                                   .authorisationTypes(Collections.emptyList())
                                   .panelPreferences(Collections.emptyList())
                                   .panelSpecialisms(Collections.emptyList())
                                   .roleType(List.of(TRIBUNAL_JUDGE))
                                   .build())
            .leadJudgeContractType("")
            .judiciary(JudiciaryModel.builder().roleType(Collections.emptyList())
               .authorisationTypes(Collections.emptyList())
               .authorisationSubType(Collections.emptyList())
               .judiciaryPreferences(Collections.emptyList())
               .judiciarySpecialisms(Collections.emptyList())
               .panelComposition(Collections.emptyList())
               .build())
            .hearingIsLinkedFlag(true)
            .parties(partyDetailsModels)
            .caseFlags(caseflags)
            .screenFlow(serviceHearingValuesProvider.getScreenFlowJson(LOCATION_OF_SCREEN_FLOW_FILE_TEST))
            .vocabulary(Collections.emptyList())
            .hearingChannels(hearingChannels)
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .build();
    }

    private ServiceHearingValuesModel buildTestBailServiceHearingValuesModel() {

        String bailListCaseHearingLength = "60";
        return ServiceHearingValuesModel.builder()
            .hmctsServiceId(serviceId)
            .hmctsInternalCaseName(caseNameHmctsInternal)
            .publicCaseName(caseReference)
            .caseCategories(List.of(bailCaseCategoryCaseType, bailCaseCategoryCaseSubType))
            .caseAdditionalSecurityFlag(false)
            .caseDeepLink(baseUrl + caseDeepLink)
            .caserestrictedFlag(false)
            .externalCaseReference(homeOfficeRef)
            .caseManagementLocationCode(HearingCentre.MANCHESTER.getEpimsId())
            .caseSlaStartDate(date.toString())
            .autoListFlag(false)
            .hearingType(bailHearingType)
            .hearingWindow(hearingWindowModel)
            .duration(Integer.parseInt(bailListCaseHearingLength))
            .hearingPriorityType(STANDARD)
            .numberOfPhysicalAttendees(0)
            .hearingInWelshFlag(false)
            .hearingLocations(Collections.emptyList())
            .facilitiesRequired(Collections.emptyList())
            .listingComments(listingComments)
            .privateHearingRequiredFlag(false)
            .caseInterpreterRequiredFlag(false)
            .hearingRequester("")
            .panelRequirements(null)
            .panelRequirements(PanelRequirementsModel.builder()
                                   .authorisationSubType(Collections.emptyList())
                                   .authorisationTypes(Collections.emptyList())
                                   .panelPreferences(Collections.emptyList())
                                   .panelSpecialisms(Collections.emptyList())
                                   .roleType(List.of(TRIBUNAL_JUDGE))
                                   .build())
            .leadJudgeContractType("")
            .judiciary(JudiciaryModel.builder().roleType(Collections.emptyList())
                           .authorisationTypes(Collections.emptyList())
                           .authorisationSubType(Collections.emptyList())
                           .judiciaryPreferences(Collections.emptyList())
                           .judiciarySpecialisms(Collections.emptyList())
                           .panelComposition(Collections.emptyList())
                           .build())
            .hearingIsLinkedFlag(false)
            .parties(partyDetailsModels)
            .caseFlags(caseflags)
            .screenFlow(serviceHearingValuesProvider.getScreenFlowJson(LOCATION_OF_SCREEN_FLOW_FILE_TEST))
            .vocabulary(Collections.emptyList())
            .hearingChannels(bailHearingChannels)
            .hearingLevelParticipantAttendance(Collections.emptyList())
            .build();
    }

    @Test
    public void should_throw_exception_when_bail_case_name_hmcts_internal_is_missing() {

        when(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceHearingValuesProvider
            .provideBailServiceHearingValues(bailCase, caseReference))
            .hasMessage("case name HMCTS internal case name is a required field")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

}
