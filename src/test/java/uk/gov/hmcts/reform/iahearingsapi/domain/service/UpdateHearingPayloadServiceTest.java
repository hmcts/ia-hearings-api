package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateHearingPayloadServiceTest {
    @Mock
    private HearingService hearingService;

    @Mock
    HearingGetResponse hearingGetResponse;
    HearingDetails hearingDetails = new HearingDetails();
    private final String updateHearingsCode = "code 1";
    UpdateHearingPayloadService updateHearingPayloadService;

    private final String locationId = "1234";
    private final String locationType = "court";
    private final String reasonCode = "hearing-type-change";
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
        updateHearingPayloadService = new UpdateHearingPayloadService(hearingService);
    }

    @Test
    void should_create_an_update_hearing_request_with_new_hearing_channels() {
        String hearingChannel = "TEL";

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            updateHearingsCode,
            Optional.of(hearingChannel),
            Optional.empty(),
            Optional.empty(),
            reasonCode,
            false,
            null
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            List.of(hearingChannel),
            updateHearingRequest.getHearingDetails().getHearingChannels()
        );
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_new_location_code() {
        String locationCode = "9876";

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            updateHearingsCode,
            Optional.empty(),
            Optional.of(locationCode),
            Optional.empty(),
            reasonCode,
            false,
            null
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);

        assertEquals(
            locationCode,
            updateHearingRequest.getHearingDetails().getHearingLocations().get(0).getLocationId()
        );
        assertEqualsHearingDetails(updateHearingRequest);
    }

    @Test
    void should_create_an_update_hearing_request_with_new_duration() {
        Integer duration = 240;

        UpdateHearingRequest updateHearingRequest = updateHearingPayloadService.createUpdateHearingPayload(
            updateHearingsCode,
            Optional.empty(),
            Optional.empty(),
            Optional.of(duration),
            reasonCode,
            false,
            null
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
            updateHearingsCode,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            reasonCode,
            true,
            null
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
            updateHearingsCode,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            reasonCode,
            false,
            hearingWindow
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
            updateHearingsCode,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            reasonCode,
            false,
            hearingWindow
        );

        verify(hearingService, times(1)).getHearing(updateHearingsCode);
        assertEquals(hearingWindow, updateHearingRequest.getHearingDetails().getHearingWindow());
        assertEqualsHearingDetails(updateHearingRequest);
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
    }
}
