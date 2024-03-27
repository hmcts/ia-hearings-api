package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_VENUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.S94B_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.Facilities.IAC_TYPE_C_CONFERENCE_EQUIPMENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.LEEDS_MAGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_INTERPRETER_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;

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
    private final String updateHearingsCode = "code 1";
    UpdateHearingPayloadService updateHearingPayloadService;

    private final String persistedHearingLocationId = "1234";
    private final String persistedHearingLocationType = "court";
    private final List<String> persistedHearingChannel = List.of("INTER");
    private final Integer persistedHearingDuration = 120;
    private final String reasonCode = ReasonCodes.OTHER.toString();
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
        when(hearingService.getHearing(updateHearingsCode)).thenReturn(persistedHearing);
        when(persistedHearing.getHearingDetails()).thenReturn(hearingDetails);
        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(partyDetailsMapper.mapAsylumPartyDetails(asylumCase,
                                                      caseFlagsMapper,
                                                      caseDataMapper,
                                                      persistedHearing.getHearingDetails(),
                                                      event))
            .thenReturn(List.of(PartyDetailsModel.builder().build()));
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
            UPDATE_INTERPRETER_DETAILS
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

        when(asylumCase.read(CHANGE_HEARING_TYPE_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YES));
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
            UPDATE_HEARING_REQUEST
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
            UPDATE_HEARING_REQUEST
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
            UPDATE_INTERPRETER_DETAILS
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
        when(asylumCase.read(CHANGE_HEARING_DURATION_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YES));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST
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
        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YES));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            true,
            null,
            UPDATE_HEARING_REQUEST
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertNull(updateHearingRequest.getHearingDetails().getHearingWindow());
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_date_fixed() {
        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YES));

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
            UPDATE_HEARING_REQUEST
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(hearingWindow, updateHearingRequest.getHearingDetails().getHearingWindow());
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_date_range() {
        when(asylumCase.read(CHANGE_HEARING_DATE_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YES));

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
            UPDATE_HEARING_REQUEST
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
            UPDATE_HEARING_REQUEST
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
        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        hearingDetails.setFacilitiesRequired(List.of("1", IAC_TYPE_C_CONFERENCE_EQUIPMENT.toString()));
        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST
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
            UPDATE_HEARING_REQUEST
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
            UPDATE_HEARING_REQUEST
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
            Event.RECORD_ADJOURNMENT_DETAILS
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
            null
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
        when(asylumCase.read(CHANGE_HEARING_LOCATION_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(CHANGE_HEARING_DURATION_YES_NO, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(updateHearingPayloadService.getHearingDuration(asylumCase, UPDATE_HEARING_REQUEST)).thenReturn(90);


        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            UPDATE_HEARING_REQUEST
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
            Event.RECORD_ADJOURNMENT_DETAILS
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

    private void setDefaultHearingDetails() {
        hearingDetails.setHearingChannels(persistedHearingChannel);
        hearingDetails.setHearingLocations(hearingLocations);
        hearingDetails.setDuration(persistedHearingDuration);
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setFacilitiesRequired(List.of("1"));
    }
}
