package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DEPORTATION_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_VENUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.S94B_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.Facilities.IAC_TYPE_C_CONFERENCE_EQUIPMENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.LEEDS_MAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_INTERPRETER_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType.HU;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PriorityType.STANDARD;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.PayloadUtils.getCaseCategoriesValue;

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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ReasonCodes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.AppealType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseTypeValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CategoryType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.ListingCommentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateHearingPayloadServiceTest {

    private static final String SERVICE_ID = "BFA1";
    private static final String BASE_URL = "baseUrl";
    @Mock
    private HearingService hearingService;
    @Mock
    private PartyDetailsMapper partyDetailsMapper;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private ListingCommentsMapper listingCommentsMapper;
    @Mock
    HearingGetResponse persistedHearing;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private PartyDetailsModel partyDetailsModel;
    @Mock
    private Event event;
    HearingDetails hearingDetails = new HearingDetails();
    CaseDetailsHearing caseDetails = new CaseDetailsHearing();
    private final String updateHearingsCode = "code 1";
    UpdateHearingPayloadService updateHearingPayloadService;

    private final String persistedHearingLocationId = "1234";
    private final String persistedHearingLocationType = "court";
    private final List<String> persistedHearingChannel = List.of("INTER");
    private final Integer persistedHearingDuration = 120;
    private final String reasonCode = ReasonCodes.OTHER.toString();
    private final Long caseReference = Long.parseLong("1717667659221764");
    private final List<HearingLocationModel> hearingLocations = List.of(HearingLocationModel
                                                                            .builder()
                                                                            .locationId(
                                                                                persistedHearingLocationId)
                                                                            .locationType(
                                                                                persistedHearingLocationType)
                                                                            .build());
    private final HearingWindowModel hearingWindow = HearingWindowModel
        .builder()
        .dateRangeStart("2023-01-15")
        .dateRangeEnd("2023-01-26")
        .build();

    @BeforeEach
    void setUp() {
        setDefaultHearingDetails();
        setDefaultCaseDetails();
        when(hearingService.getHearing(updateHearingsCode)).thenReturn(persistedHearing);
        when(persistedHearing.getHearingDetails()).thenReturn(hearingDetails);
        when(persistedHearing.getCaseDetails()).thenReturn(caseDetails);
        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(HU));
        when(partyDetailsMapper.mapAsylumPartyDetails(asylumCase,
                                                      caseFlagsMapper,
                                                      caseDataMapper,
                                                      persistedHearing.getHearingDetails(),
                                                      event))
            .thenReturn(List.of(PartyDetailsModel.builder().build()));

        when(caseFlagsMapper.getHearingPriorityType(asylumCase)).thenReturn(STANDARD);
        when(listingCommentsMapper.getListingComments(asylumCase, caseFlagsMapper, caseDataMapper))
            .thenReturn("test listing comments");

        when(caseFlagsMapper.getPrivateHearingRequiredFlag(asylumCase)).thenReturn(true);
        when(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)).thenReturn(Optional.of("internalCaseName"));

        updateHearingPayloadService = new UpdateHearingPayloadService(
            caseDataMapper,
            caseFlagsMapper,
            partyDetailsMapper,
            listingCommentsMapper,
            SERVICE_ID,
            BASE_URL,
            hearingService
        );
    }

    @Test
    void should_create_an_update_hearing_request_with_new_hearing_channels() {

        when(caseDataMapper.getHearingChannels(asylumCase,
                                               persistedHearing.getHearingDetails(),
                                               UPDATE_INTERPRETER_DETAILS))
            .thenReturn(List.of("INTER"));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_INTERPRETER_DETAILS,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            persistedHearingChannel,
            updateHearingRequest.getHearingDetails().getHearingChannels()
        );
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_hearing_channels_set_to_on_the_papers() {

        when(asylumCase.read(CHANGE_HEARING_TYPE_YES_NO, String.class)).thenReturn(Optional.of(YES.toString()));
        when(caseDataMapper.getHearingChannels(asylumCase,
                                               persistedHearing.getHearingDetails(),
                                               UPDATE_HEARING_REQUEST))
            .thenReturn(List.of("ONPPRS"));


        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        assertEquals(
            List.of(HearingChannel.ONPPRS.name()),
            updateHearingRequest.getHearingDetails().getHearingChannels()
        );
    }

    @Test
    void should_create_an_update_hearing_request_with_auto_list_flag_set_to_false() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(DECISION_WITHOUT_HEARING));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        assertFalse(updateHearingRequest.getHearingDetails().isAutolistFlag());
    }

    @Test
    void should_create_an_update_hearing_request_with_auto_list_flag_set_to_false_when_ref_data_enabled() {

        when(asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        assertFalse(updateHearingRequest.getHearingDetails().isAutolistFlag());
    }

    @Test
    void should_create_an_update_hearing_request_with_new_location_code() {

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_INTERPRETER_DETAILS,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            persistedHearingLocationId,
            updateHearingRequest.getHearingDetails().getHearingLocations().get(0).getLocationId()
        );
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_new_duration() {
        Integer duration = 240;
        when(updateHearingPayloadService.getHearingDuration(asylumCase, UPDATE_HEARING_REQUEST)).thenReturn(duration);
        when(asylumCase.read(CHANGE_HEARING_DURATION_YES_NO, String.class)).thenReturn(Optional.of(YES.toString()));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            duration,
            updateHearingRequest.getHearingDetails().getDuration()
        );
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_first_available_date() {
        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)).thenReturn(Optional.of(YES.toString()));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            true,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertNull(updateHearingRequest.getHearingDetails().getHearingWindow());
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_date_fixed() {
        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)).thenReturn(Optional.of(YES.toString()));

        HearingWindowModel hearingWindow = HearingWindowModel
            .builder()
            .dateRangeStart("2023-12-02T00:00")
            .build();

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            hearingWindow,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(hearingWindow, updateHearingRequest.getHearingDetails().getHearingWindow());
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_date_range() {
        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)).thenReturn(Optional.of(YES.toString()));

        HearingWindowModel hearingWindow = HearingWindowModel
            .builder()
            .dateRangeStart("2023-03-10")
            .dateRangeEnd("2023-03-30")
            .build();

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            hearingWindow,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(hearingWindow, updateHearingRequest.getHearingDetails().getHearingWindow());
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_set_iac_type_c_conference_equipment_facility_when_s94b_is_enabled() {
        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            List.of("1", IAC_TYPE_C_CONFERENCE_EQUIPMENT.toString()),
            updateHearingRequest.getHearingDetails().getFacilitiesRequired()
        );
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_remove_iac_type_c_conference_equipment_facility_when_s94b_is_disabled() {
        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(NO));
        hearingDetails.setFacilitiesRequired(List.of("1", IAC_TYPE_C_CONFERENCE_EQUIPMENT.toString()));
        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            List.of("1"),
            updateHearingRequest.getHearingDetails().getFacilitiesRequired()
        );
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_include_party_details_information() {
        List<PartyDetailsModel> partyDetails = List.of(PartyDetailsModel.builder()
                                                           .partyName("MyPartyName")
                                                           .partyID("MyPartyId")
                                                           .partyType(PartyType.IND.getPartyType())
                                                           .build());
        when(partyDetailsMapper.mapAsylumPartyDetails(asylumCase,
                                                      caseFlagsMapper,
                                                      caseDataMapper,
                                                      persistedHearing.getHearingDetails(),
                                                      UPDATE_HEARING_REQUEST))
            .thenReturn(partyDetails);

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        verify(partyDetailsMapper, times(1)).mapAsylumPartyDetails(asylumCase,
                                                                   caseFlagsMapper,
                                                                   caseDataMapper,
                                                                   persistedHearing.getHearingDetails(),
                                                                   UPDATE_HEARING_REQUEST);

        assertEquals(partyDetails, updateHearingRequest.getPartyDetails());
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_updated_party_details() {
        when(partyDetailsMapper.mapAsylumPartyDetails(asylumCase,
                                                      caseFlagsMapper,
                                                      caseDataMapper,
                                                      persistedHearing.getHearingDetails(),
                                                      UPDATE_HEARING_REQUEST))
            .thenReturn(List.of(partyDetailsModel));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            true,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        assertEquals(List.of(partyDetailsModel), updateHearingRequest.getPartyDetails());
    }


    @Test
    void should_create_an_update_hearing_request_for_adjournment_details_cases() {
        String hearingChannel = "TEL";
        when(caseDataMapper.getHearingChannels(asylumCase,
                                               persistedHearing.getHearingDetails(),
                                               Event.RECORD_ADJOURNMENT_DETAILS))
            .thenReturn(List.of(hearingChannel));


        when(asylumCase.read(NEXT_HEARING_VENUE, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("569737")));
        Integer duration = 240;
        when(updateHearingPayloadService.getHearingDuration(asylumCase, Event.RECORD_ADJOURNMENT_DETAILS))
            .thenReturn(duration);


        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            Event.RECORD_ADJOURNMENT_DETAILS,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            List.of(hearingChannel),
            updateHearingRequest.getHearingDetails().getHearingChannels()
        );
        assertEquals(
            LEEDS_MAGS.getEpimsId(),
            updateHearingRequest.getHearingDetails().getHearingLocations().get(0).getLocationId()
        );
        assertEquals(
            duration,
            updateHearingRequest.getHearingDetails().getDuration()
        );
    }

    private void assertEqualsHearingDetails(UpdateHearingRequest updateHearingRequestSent) {
        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            hearingDetails.getHearingChannels(),
            updateHearingRequestSent.getHearingDetails().getHearingChannels()
        );
        assertEquals(
            hearingDetails.getHearingLocations(),
            updateHearingRequestSent.getHearingDetails().getHearingLocations()
        );
        assertEquals(
            hearingDetails.getHearingWindow(),
            updateHearingRequestSent.getHearingDetails().getHearingWindow()
        );
        assertEquals(hearingDetails.getDuration(), updateHearingRequestSent.getHearingDetails().getDuration());
        assertEquals(
            hearingDetails.getAmendReasonCodes(),
            updateHearingRequestSent.getHearingDetails().getAmendReasonCodes()
        );
        assertEquals(
            STANDARD.toString(),
            updateHearingRequestSent.getHearingDetails().getHearingPriorityType()
        );
        assertEquals(
            "test listing comments",
            updateHearingRequestSent.getHearingDetails().getListingComments()
        );
        assertTrue(updateHearingRequestSent.getHearingDetails().getPrivateHearingRequiredFlag());
    }

    @Test
    void should_return_default_hearing_value_when_the_event_is_null() {
        when(caseDataMapper.getHearingChannels(asylumCase, persistedHearing.getHearingDetails(), null))
            .thenReturn(persistedHearingChannel);

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            null,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            persistedHearingChannel,
            updateHearingRequest.getHearingDetails().getHearingChannels());

        assertEquals(
            persistedHearingLocationId,
            updateHearingRequest.getHearingDetails().getHearingLocations().get(0).getLocationId());

        assertEquals(
            persistedHearingDuration,
            updateHearingRequest.getHearingDetails().getDuration());

        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_return_requested_update_hearing_value_when_the_event_is_update_hearing_request() {
        when(asylumCase.read(HEARING_LOCATION, DynamicList.class)).thenReturn(
            Optional.of(new DynamicList("783803")));
        when(asylumCase.read(CHANGE_HEARING_LOCATION_YES_NO, String.class)).thenReturn(Optional.of(YES.toString()));
        when(asylumCase.read(CHANGE_HEARING_DURATION_YES_NO, String.class)).thenReturn(Optional.of(YES.toString()));
        when(updateHearingPayloadService.getHearingDuration(asylumCase, UPDATE_HEARING_REQUEST)).thenReturn(90);


        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            "783803",
            updateHearingRequest.getHearingDetails().getHearingLocations().get(0).getLocationId());

        assertEquals(
            90,
            updateHearingRequest.getHearingDetails().getDuration());

        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_return_next_hearing_value_when_the_event_is_record_adjournment_details() {

        when(asylumCase.read(NEXT_HEARING_VENUE, DynamicList.class)).thenReturn(
            Optional.of(new DynamicList("227101")));
        when(updateHearingPayloadService.getHearingDuration(asylumCase, Event.RECORD_ADJOURNMENT_DETAILS))
            .thenReturn(60);

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            Event.RECORD_ADJOURNMENT_DETAILS,
            caseReference
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            HearingCentre.NEWPORT.getEpimsId(),
            updateHearingRequest.getHearingDetails().getHearingLocations().get(0).getLocationId());

        assertEquals(
            60,
            updateHearingRequest.getHearingDetails().getDuration());

        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_set_case_details_with_latest_values() {
        when(caseFlagsMapper.getPublicCaseName(asylumCase, caseReference.toString()))
            .thenReturn(caseReference.toString());

        when(caseFlagsMapper.getCaseInterpreterRequiredFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCaseAdditionalSecurityFlag(asylumCase)).thenReturn(true);
        when(caseDataMapper.getCaseManagementLocationCode(asylumCase)).thenReturn("glasgow");
        when(asylumCase.read(DEPORTATION_ORDER_OPTIONS, YesOrNo.class)).thenReturn(Optional.of(YES));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            Event.RECORD_ADJOURNMENT_DETAILS,
            caseReference
        );

        assertTrue(updateHearingRequest.getCaseDetails().isCaseInterpreterRequiredFlag());
        assertTrue(updateHearingRequest.getCaseDetails().isCaseAdditionalSecurityFlag());
        assertEquals(
            "glasgow",
            updateHearingRequest.getCaseDetails().getCaseManagementLocationCode());

        assertEquals(
            getCaseCategoriesValue(asylumCase),
            updateHearingRequest.getCaseDetails().getCaseCategories());

        assertEquals(
            "internalCaseName",
            updateHearingRequest.getCaseDetails().getHmctsInternalCaseName());
    }

    @Test
    void should_set_hearing_window_to_null_if_persisted_hearing_window_all_null() {
        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)).thenReturn(Optional.of(NO.toString()));

        hearingDetails.setHearingWindow(HearingWindowModel
            .builder()
            .dateRangeStart(null)
            .dateRangeEnd(null)
            .firstDateTimeMustBe(null)
            .build());

        when(persistedHearing.getHearingDetails()).thenReturn(hearingDetails);

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            hearingWindow,
            UPDATE_HEARING_REQUEST,
            caseReference
        );

        assertNull(updateHearingRequest.getHearingDetails().getHearingWindow());
    }

    private void setDefaultHearingDetails() {
        hearingDetails.setHearingChannels(persistedHearingChannel);
        hearingDetails.setHearingLocations(hearingLocations);
        hearingDetails.setDuration(persistedHearingDuration);
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setFacilitiesRequired(List.of("1"));
    }

    private void setDefaultCaseDetails() {
        CaseCategoryModel caseCategoryModel = new CaseCategoryModel();
        caseCategoryModel.setCategoryType(CategoryType.CASE_TYPE);
        caseCategoryModel.setCategoryValue(CaseTypeValue.RPD.getValue());
        caseCategoryModel.setCategoryParent("");

        caseDetails.setPublicCaseName("Appellant name");
        caseDetails.setCaseInterpreterRequiredFlag(false);
        caseDetails.setCaseAdditionalSecurityFlag(false);
        caseDetails.setCaseCategories(List.of(caseCategoryModel));
        caseDetails.setCaseManagementLocationCode("manchester");
    }
}
