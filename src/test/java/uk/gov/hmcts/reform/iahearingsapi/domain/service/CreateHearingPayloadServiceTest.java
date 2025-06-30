package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_HEARING_LINKED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType.STANDARD;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BaseLocation;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PanelRequirementsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.ListingCommentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.PayloadUtils;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CreateHearingPayloadServiceTest {

    private static final Integer LIST_CASE_HEARING_LENGTH = 120;
    private static final String SUBSTANTIVE_HEARING_TYPE = "BFA1-SUB";
    private static final List<String> HEARING_CHANNELS = List.of("INTER");
    private static final String LOCATION_TYPE_COURT = "court";
    private static final String BIRMINGHAM_ID = "231596";
    private static final String TRIBUNAL_JUDGE = "84";
    private static final LocalDate DATE_START = LocalDate.of(2023, 8, 1);
    private static final LocalDate DATE_END = LocalDate.of(2023, 8, 15);
    private static final String LISTING_COMMENTS = "Customer behaviour: unfriendly";
    private static final String HMCTS_CASE_NAME_INTERNAL = "Eke Uke";
    private static final String CASE_REFERENCE = "1234567891234567";
    private static final long CASE_REFERENCE_L = 1234567891234567L;
    private static final String HOME_OFFICE_REFERENCE = "homeOfficeRef";
    private static final String CASE_DEEP_LINK = "/cases/case-details/1234567891234567#Overview";
    private static final CaseCategoryModel CASE_CATEGORY_CASE_TYPE = new CaseCategoryModel();
    private static final CaseCategoryModel CASE_CATEGORY_CASE_SUBTYPE = new CaseCategoryModel();
    private static final String BASE_URL = "http://localhost:3002";
    private static final String SERVICE_ID = "BFA1";
    private static final List<PartyDetailsModel> PARTY_DETAILS_MODELS = Arrays.asList(
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build(),
        PartyDetailsModel.builder().build()
    );
    private static final HearingWindowModel HEARING_WINDOW_MODEL = HearingWindowModel.builder()
        .dateRangeStart(DATE_START.toString())
        .dateRangeEnd(DATE_END.toString())
        .build();

    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private PartyDetailsMapper partyDetailsMapper;
    @Mock
    private ListingCommentsMapper listingCommentsMapper;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private CreateHearingPayloadService createHearingPayloadService;
    private final MockedStatic<PayloadUtils> payloadUtils = mockStatic(PayloadUtils.class);

    @BeforeEach
    void setup() {
        createHearingPayloadService = new CreateHearingPayloadService(
            caseDataMapper,
            caseFlagsMapper,
            partyDetailsMapper,
            listingCommentsMapper,
            SERVICE_ID,
            BASE_URL
        );
    }

    @AfterEach
    void tearDown() {
        payloadUtils.close();
    }

    @Test
    void should_get_hmc_hearing_request_payload() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(CASE_REFERENCE_L);
        when(asylumCase.read(AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL, String.class))
            .thenReturn(Optional.of(HMCTS_CASE_NAME_INTERNAL));
        when(asylumCase.read(IS_HEARING_LINKED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(partyDetailsMapper.mapAsylumPartyDetails(asylumCase,
                                                      caseFlagsMapper,
                                                      caseDataMapper)).thenReturn(PARTY_DETAILS_MODELS);
        when(caseDataMapper.getHearingDuration(asylumCase)).thenReturn(LIST_CASE_HEARING_LENGTH);
        when(caseDataMapper.getHearingChannels(asylumCase)).thenReturn(HEARING_CHANNELS);
        when(caseDataMapper.getCaseManagementLocationCode(asylumCase)).thenReturn(BIRMINGHAM_ID);
        when(caseFlagsMapper.getHearingPriorityType(asylumCase)).thenReturn(STANDARD);
        when(caseDataMapper.getHearingWindowModel(true))
            .thenReturn(HEARING_WINDOW_MODEL);
        when(listingCommentsMapper.getListingComments(asylumCase,
                                                      caseFlagsMapper,
                                                      caseDataMapper))
            .thenReturn(LISTING_COMMENTS);

        payloadUtils.when(() -> PayloadUtils.getNumberOfPhysicalAttendees(PARTY_DETAILS_MODELS))
            .thenReturn(0);
        payloadUtils.when(() -> PayloadUtils.getCaseCategoriesValue(asylumCase))
            .thenReturn(List.of(CASE_CATEGORY_CASE_TYPE, CASE_CATEGORY_CASE_SUBTYPE));

        when(caseFlagsMapper.getPrivateHearingRequiredFlag(asylumCase)).thenReturn(true);
        when(caseDataMapper.getExternalCaseReference(asylumCase)).thenReturn(HOME_OFFICE_REFERENCE);
        when(caseDataMapper.getCaseDeepLink(CASE_REFERENCE)).thenReturn(CASE_DEEP_LINK);
        when(caseFlagsMapper.getPublicCaseName(asylumCase, CASE_REFERENCE)).thenReturn(CASE_REFERENCE);
        when(caseFlagsMapper.getCaseAdditionalSecurityFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCaseInterpreterRequiredFlag(asylumCase)).thenReturn(true);
        when(caseDataMapper.getCaseManagementLocationCode(asylumCase)).thenReturn(BaseLocation.BIRMINGHAM.getId());
        when(caseDataMapper.getCaseSlaStartDate()).thenReturn(DATE_START);
        when(caseDataMapper.getHearingLinkedFlag(asylumCase)).thenReturn(true);

        CreateHearingRequest expected = buildTestAsylumCreateHearingRequest();
        CreateHearingRequest actual = createHearingPayloadService.buildCreateHearingRequest(caseDetails);

        assertEquals(expected, actual);
    }

    private CreateHearingRequest buildTestAsylumCreateHearingRequest() {

        HearingDetails hearingDetails = HearingDetails.builder()
            .duration(LIST_CASE_HEARING_LENGTH)
            .hearingType(SUBSTANTIVE_HEARING_TYPE)
            .hearingChannels(HEARING_CHANNELS)
            .autolistFlag(false)
            .facilitiesRequired(Collections.emptyList())
            .hearingInWelshFlag(false)
            .hearingLocations(List.of(HearingLocationModel.builder()
                                          .locationId(BIRMINGHAM_ID)
                                          .locationType(LOCATION_TYPE_COURT).build()))
            .panelRequirements(PanelRequirementsModel.builder()
                                   .authorisationSubType(Collections.emptyList())
                                   .authorisationTypes(Collections.emptyList())
                                   .panelPreferences(Collections.emptyList())
                                   .panelSpecialisms(Collections.emptyList())
                                   .roleType(List.of(TRIBUNAL_JUDGE))
                                   .build())
            .hearingRequester("")
            .hearingPriorityType(STANDARD.toString())
            .hearingWindow(HEARING_WINDOW_MODEL)
            .multiDayHearing(false)
            .listingComments(LISTING_COMMENTS)
            .numberOfPhysicalAttendees(0)
            .privateHearingRequiredFlag(true)
            .hearingIsLinkedFlag(true)
            .build();

        CaseDetailsHearing caseDetails =
            CaseDetailsHearing.builder()
                .hmctsServiceCode(SERVICE_ID)
                .caseRef(CASE_REFERENCE)
                .externalCaseReference(HOME_OFFICE_REFERENCE)
                .caseDeepLink(BASE_URL + CASE_DEEP_LINK)
                .hmctsInternalCaseName(HMCTS_CASE_NAME_INTERNAL)
                .publicCaseName(CASE_REFERENCE)
                .caseAdditionalSecurityFlag(true)
                .caseInterpreterRequiredFlag(true)
                .caseCategories(List.of(CASE_CATEGORY_CASE_TYPE, CASE_CATEGORY_CASE_SUBTYPE))
                .caseManagementLocationCode(BaseLocation.BIRMINGHAM.getId())
                .caseRestrictedFlag(false)
                .caseSlaStartDate(DATE_START)
                .build();

        return CreateHearingRequest.builder()
            .caseDetails(caseDetails)
            .hearingDetails(hearingDetails)
            .partyDetails(PARTY_DETAILS_MODELS)
            .build();
    }
}
