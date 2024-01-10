package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_FORMAT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.S94B_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.Facilities.IAC_TYPE_C_CONFERENCE_EQUIPMENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.LEEDS_MAGS;

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
    HearingGetResponse hearingGetResponse;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private PartyDetailsModel partyDetailsModel;
    HearingDetails hearingDetails = new HearingDetails();
    private final String updateHearingsCode = "code 1";
    UpdateHearingPayloadService updateHearingPayloadService;

    private final String locationId = "1234";
    private final String locationType = "court";
    private final String reasonCode = ReasonCodes.OTHER.toString();
    private final List<HearingLocationModel> hearingLocations = List.of(HearingLocationModel
                                                                            .builder()
                                                                            .locationId(locationId)
                                                                            .locationType(locationType)
                                                                            .build());
    private final HearingWindowModel hearingWindow = HearingWindowModel
        .builder()
        .dateRangeStart("2023-01-15")
        .dateRangeEnd("2023-01-26")
        .build();

    @BeforeEach
    void setUp() {
        setDefaultHearingDetails();
        when(hearingService.getHearing(updateHearingsCode)).thenReturn(hearingGetResponse);
        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        when(asylumCase.read(S94B_STATUS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(partyDetailsMapper.mapAsylumPartyDetails(asylumCase, caseFlagsMapper, caseDataMapper))
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
        String hearingChannel = "TEL";
        when(asylumCase.read(
            HEARING_CHANNEL,
            DynamicList.class
        )).thenReturn(Optional.of(new DynamicList(hearingChannel)));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            false
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            List.of(hearingChannel),
            updateHearingRequest.getHearingDetails().getHearingChannels()
        );
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_hearing_channels_set_to_on_the_papers() {

        when(caseDataMapper.isDecisionWithoutHearingAppeal(asylumCase))
            .thenReturn(true);

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            false
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
            false
        );

        assertFalse(updateHearingRequest.getHearingDetails().isAutolistFlag());
    }

    @Test
    void should_create_an_update_hearing_request_with_new_location_code() {

        when(asylumCase.read(
            LIST_CASE_HEARING_CENTRE,
            HearingCentre.class
        )).thenReturn(Optional.of(HearingCentre.BRADFORD));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            false
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            HearingCentre.BRADFORD.getEpimsId(),
            updateHearingRequest.getHearingDetails().getHearingLocations().get(0).getLocationId()
        );
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_new_duration() {
        Integer duration = 240;
        when(caseDataMapper.getHearingDuration(asylumCase, false)).thenReturn(duration);

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            false
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
        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            true,
            null,
            false
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertNull(updateHearingRequest.getHearingDetails().getHearingWindow());
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_date_fixed() {
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
            false
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(hearingWindow, updateHearingRequest.getHearingDetails().getHearingWindow());
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_date_range() {
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
            false
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
            false
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
            false
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
        when(partyDetailsMapper.mapAsylumPartyDetails(asylumCase, caseFlagsMapper, caseDataMapper))
            .thenReturn(partyDetails);

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            false
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        verify(partyDetailsMapper, times(1)).mapAsylumPartyDetails(asylumCase, caseFlagsMapper, caseDataMapper);

        assertEquals(partyDetails, updateHearingRequest.getPartyDetails());
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_updated_party_details() {
        when(partyDetailsMapper.mapAsylumPartyDetails(asylumCase, caseFlagsMapper, caseDataMapper))
            .thenReturn(List.of(partyDetailsModel));

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            true,
            null,
            false
        );

        assertEquals(List.of(partyDetailsModel), updateHearingRequest.getPartyDetails());
    }


    @Test
    void should_create_an_update_hearing_request_for_adjournment_details_cases() {
        String hearingChannel = "TEL";
        when(asylumCase.read(
            NEXT_HEARING_FORMAT,
            DynamicList.class
        )).thenReturn(Optional.of(new DynamicList(hearingChannel)));

        when(asylumCase.read(NEXT_HEARING_LOCATION, String.class))
            .thenReturn(Optional.of(LEEDS_MAGS.getValue()));
        Integer duration = 240;
        when(caseDataMapper.getHearingDuration(asylumCase, true)).thenReturn(duration);


        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode,
            false,
            null,
            true
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

    private void setDefaultHearingDetails() {
        hearingDetails.setHearingChannels(List.of("INTER"));
        hearingDetails.setHearingLocations(hearingLocations);
        hearingDetails.setDuration(120);
        hearingDetails.setHearingWindow(hearingWindow);
        hearingDetails.setFacilitiesRequired(List.of("1"));
    }
}
