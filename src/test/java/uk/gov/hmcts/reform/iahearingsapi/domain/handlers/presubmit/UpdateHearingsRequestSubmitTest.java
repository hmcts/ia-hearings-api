package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_UPDATE_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateHearingsRequestSubmitTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private HearingService hearingService;
    @Mock
    HearingGetResponse hearingGetResponse;
    HearingDetails hearingDetails = new HearingDetails();
    private final String updateHearingsCode = "code 1";
    UpdateHearingRequestSubmit updateHearingRequestSubmit;

    private final String locationId = "1234";
    private final String locationType = "court";
    private final DynamicList reasonCode = new DynamicList(new Value(
        "hearing-type-change",
        "Different hearing mode required"
    ), null);
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

    @Captor
    private ArgumentCaptor<UpdateHearingRequest> updateHearingRequestArgumentCaptor;
    private AsylumCase asylumCase;

    @BeforeEach
    void setUp() {

        asylumCase = new AsylumCase();
        DynamicList dynamicListOfHearings = new DynamicList(updateHearingsCode);
        asylumCase.write(CHANGE_HEARINGS, dynamicListOfHearings);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(hearingService.getHearing(updateHearingsCode)).thenReturn(hearingGetResponse);
        when(hearingService.updateHearing(any(UpdateHearingRequest.class), any())).thenReturn(
            hearingGetResponse);

        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        updateHearingRequestSubmit = new UpdateHearingRequestSubmit(hearingService);
    }

    @Test
    void should_send_an_update_of_the_hearing_channels() {
        setDefaultHearingDetails();

        asylumCase.write(HEARING_CHANNEL, "TEL");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1))
            .updateHearing(updateHearingRequestArgumentCaptor.capture(), any());
        UpdateHearingRequest updateHearingRequestSent = updateHearingRequestArgumentCaptor.getValue();

        assertEquals(
            List.of("TEL"),
            updateHearingRequestSent.getHearingDetails().getHearingChannels()
        );
        assertEqualsHearingDetails(updateHearingRequestSent);
        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_locations() {
        setDefaultHearingDetails();

        asylumCase.write(LIST_CASE_HEARING_CENTRE, new DynamicList(new Value("9999", "9999"), null));
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1))
            .updateHearing(updateHearingRequestArgumentCaptor.capture(), any());
        UpdateHearingRequest updateHearingRequestSent = updateHearingRequestArgumentCaptor.getValue();

        List<HearingLocationModel> newHearingLocation = List.of(HearingLocationModel
                                                                    .builder()
                                                                    .locationId("9999")
                                                                    .locationType("court")
                                                                    .build());

        assertEquals(
            newHearingLocation,
            updateHearingRequestSent.getHearingDetails().getHearingLocations()
        );
        assertEqualsHearingDetails(updateHearingRequestSent);
        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_window_date_to_be_fixed() {
        setDefaultHearingDetails();
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "DateToBeFixed");
        asylumCase.write(LIST_CASE_HEARING_DATE, "2023-12-02");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1))
            .updateHearing(updateHearingRequestArgumentCaptor.capture(), any());
        UpdateHearingRequest updateHearingRequestSent = updateHearingRequestArgumentCaptor.getValue();
        HearingWindowModel newHearingWindow = HearingWindowModel
            .builder()
            .firstDateTimeMustBe("2023-12-02T00:00")
            .build();

        assertEquals(
            newHearingWindow,
            updateHearingRequestSent.getHearingDetails().getHearingWindow()
        );
        assertEqualsHearingDetails(updateHearingRequestSent);
        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_window_choose_a_date_range() {
        setDefaultHearingDetails();
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "ChooseADateRange");
        asylumCase.write(CHANGE_HEARING_DATE_RANGE_EARLIEST, "2023-12-02");
        asylumCase.write(CHANGE_HEARING_DATE_RANGE_LATEST, "2023-12-26");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1))
            .updateHearing(updateHearingRequestArgumentCaptor.capture(), any());
        UpdateHearingRequest updateHearingRequestSent = updateHearingRequestArgumentCaptor.getValue();
        HearingWindowModel newHearingWindow = HearingWindowModel
            .builder()
            .dateRangeStart("2023-12-02")
            .dateRangeEnd("2023-12-26")
            .build();
        assertEquals(
            newHearingWindow,
            updateHearingRequestSent.getHearingDetails().getHearingWindow()
        );
        assertEqualsHearingDetails(updateHearingRequestSent);
        verifyFieldsAreCleared();
    }

    @Test
    void should_throw_an_exception_if_no_start_date_range_is_set() {
        setDefaultHearingDetails();
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "DateToBeFixed");
        assertThrows(
            IllegalStateException.class,
            () -> updateHearingRequestSubmit.handle(ABOUT_TO_SUBMIT, callback),
            "LIST_CASE_HEARING_DATE type is not present"
        );
    }

    @Test
    void should_throw_an_exception_if_no_fixed_date_is_set() {
        setDefaultHearingDetails();
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "ChooseADateRange");
        assertThrows(
            IllegalStateException.class,
            () -> updateHearingRequestSubmit.handle(ABOUT_TO_SUBMIT, callback),
            "CHOOSE_A_DATE_RANGE_EARLIEST type is not present"
        );
    }

    @Test
    void should_throw_an_exception_if_no_latest_date_range_is_set() {
        setDefaultHearingDetails();
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "ChooseADateRange");
        asylumCase.write(CHANGE_HEARING_DATE_RANGE_EARLIEST, "2023-12-02");
        assertThrows(
            IllegalStateException.class,
            () -> updateHearingRequestSubmit.handle(ABOUT_TO_SUBMIT, callback),
            "CHOOSE_A_DATE_RANGE_LATEST type is not present"
        );
    }

    @Test
    void should_send_an_update_of_the_hearing_duration() {
        setDefaultHearingDetails();

        asylumCase.write(LIST_CASE_HEARING_LENGTH, new DynamicList(new Value("240", "240"), null));
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1))
            .updateHearing(updateHearingRequestArgumentCaptor.capture(), any());
        UpdateHearingRequest updateHearingRequestSent = updateHearingRequestArgumentCaptor.getValue();

        assertEquals(
            240,
            updateHearingRequestSent.getHearingDetails().getDuration()
        );
        assertEqualsHearingDetails(updateHearingRequestSent);
        verifyFieldsAreCleared();
    }

    @Test
    void should_throw_an_exception_if_there_are_no_reason_codes() {
        setDefaultHearingDetails();
        assertThrows(
            IllegalStateException.class,
            () -> updateHearingRequestSubmit.handle(ABOUT_TO_SUBMIT, callback),
            "HEARING_UPDATE_REASON_LIST type is not present"
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
    }

    private void verifyFieldsAreCleared() {
        assertEquals(asylumCase.read(CHANGE_HEARINGS), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_TYPE_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(HEARING_CHANNEL), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_LOCATION_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(LIST_CASE_HEARING_CENTRE), Optional.empty());
        assertEquals(asylumCase.read(LIST_CASE_HEARING_LENGTH), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DURATION_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(CHANGE_HEARING_UPDATE_REASON), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_TYPE), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(LIST_CASE_HEARING_DATE), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_RANGE_EARLIEST), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_RANGE_LATEST), Optional.empty());
    }

}
