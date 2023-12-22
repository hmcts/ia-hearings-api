package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ReasonCodes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_UPDATE_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;


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
    UpdateHearingPayloadService updateHearingPayloadService;
    @Mock
    HearingGetResponse hearingGetResponse;
    @Mock
    UpdateHearingRequest updateHearingRequest;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    HearingDetails hearingDetails = new HearingDetails();
    private final String updateHearingsCode = "code 1";
    UpdateHearingRequestSubmit updateHearingRequestSubmit;

    private final DynamicList reasonCode = new DynamicList(new Value(
        ReasonCodes.OTHER.name(),
        ReasonCodes.OTHER.toString()
    ), null);

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
        when(hearingService.updateHearing(any(UpdateHearingRequest.class), any())).thenReturn(new HearingGetResponse());
        when(updateHearingRequest.getHearingDetails()).thenReturn(hearingDetails);
        when(updateHearingPayloadService.createUpdateHearingPayload(
            any(),
            any(),
            any(),
            any(),
            any()
        )).thenReturn(updateHearingRequest);

        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        updateHearingRequestSubmit = new UpdateHearingRequestSubmit(
            hearingService, updateHearingPayloadService, coreCaseDataService);
    }

    @Test
    void should_send_an_update_of_the_hearing_channels() {

        asylumCase.write(HEARING_CHANNEL, "TEL");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            null
        );

        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_locations() {
        asylumCase.write(LIST_CASE_HEARING_CENTRE, HearingCentre.BRADFORD);
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            null
        );

        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_window_date_to_be_fixed() {
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "DateToBeFixed");
        asylumCase.write(LIST_CASE_HEARING_DATE, "2023-12-02T00:00:00.000");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());
        HearingWindowModel newHearingWindow = HearingWindowModel
            .builder()
            .firstDateTimeMustBe("2023-12-02T00:00")
            .build();

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            newHearingWindow
        );
        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_window_choose_a_date_range() {
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "ChooseADateRange");
        asylumCase.write(CHANGE_HEARING_DATE_RANGE_EARLIEST, "2023-12-02");
        asylumCase.write(CHANGE_HEARING_DATE_RANGE_LATEST, "2023-12-26");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        HearingWindowModel newHearingWindow = HearingWindowModel
            .builder()
            .dateRangeStart("2023-12-02")
            .dateRangeEnd("2023-12-26")
            .build();

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            newHearingWindow
        );
        verifyFieldsAreCleared();
    }

    @Test
    void should_send_an_update_of_the_hearing_window_first_available_date() {
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "FirstAvailableDate");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService, times(1)).updateHearing(any(), any());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            true,
            null
        );
        verifyFieldsAreCleared();
    }

    @Test
    void should_throw_an_exception_if_no_start_date_range_is_set() {
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        asylumCase.write(CHANGE_HEARING_DATE_TYPE, "DateToBeFixed");
        assertThrows(
            IllegalStateException.class,
            () -> updateHearingRequestSubmit.handle(ABOUT_TO_SUBMIT, callback),
            "DATE_TO_BE_FIXED_VALUE type is not present"
        );
    }

    @Test
    void should_throw_an_exception_if_no_fixed_date_is_set() {
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
        asylumCase.write(LIST_CASE_HEARING_LENGTH, "240");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );
        assertEquals(asylumCase, callbackResponse.getData());

        verify(updateHearingPayloadService, times(1)).createUpdateHearingPayload(
            asylumCase,
            updateHearingsCode,
            reasonCode.getValue().getCode(),
            false,
            null
        );
        verifyFieldsAreCleared();
    }

    @Test
    void should_throw_an_exception_if_there_are_no_reason_codes() {
        assertThrows(
            IllegalStateException.class,
            () -> updateHearingRequestSubmit.handle(ABOUT_TO_SUBMIT, callback),
            "HEARING_UPDATE_REASON_LIST type is not present"
        );
    }

    @Test
    void should_require_manual_update_hearing_if_update_hearing_call_failed() {
        asylumCase.write(HEARING_CHANNEL, "TEL");
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        when(hearingService.updateHearing(any(UpdateHearingRequest.class), any()))
            .thenThrow(new HmcException(new Throwable()));

        updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );

        assertThat(asylumCase.read(MANUAL_UPDATE_HEARING_REQUIRED), samePropertyValuesAs(Optional.of(YES)));
    }


    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void should_set_trigger_review_interpreter_booking_task(boolean shouldTriggerTask) {
        asylumCase.write(CHANGE_HEARING_UPDATE_REASON, reasonCode);
        when(updateHearingPayloadService.shouldTriggerReviewInterpreterBookingTask(
            any(), any())).thenReturn(shouldTriggerTask);

        updateHearingRequestSubmit.handle(
            ABOUT_TO_SUBMIT,
            callback
        );

        Optional expectedValue = shouldTriggerTask ? Optional.of(YES) : Optional.empty();
        assertThat(asylumCase.read(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK), samePropertyValuesAs(expectedValue));
    }

    private void verifyFieldsAreCleared() {
        assertEquals(asylumCase.read(CHANGE_HEARINGS), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_TYPE_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(CHANGE_HEARING_LOCATION_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(CHANGE_HEARING_DURATION_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(CHANGE_HEARING_UPDATE_REASON), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_TYPE), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_YES_NO), Optional.of("no"));
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_RANGE_EARLIEST), Optional.empty());
        assertEquals(asylumCase.read(CHANGE_HEARING_DATE_RANGE_LATEST), Optional.empty());
    }

}
