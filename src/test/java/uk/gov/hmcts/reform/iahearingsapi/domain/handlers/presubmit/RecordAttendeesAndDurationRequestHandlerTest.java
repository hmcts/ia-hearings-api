package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_START_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_END;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_START;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActuals;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingOutcome;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingResult;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PauseDateTime;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAttendeesAndDurationRequestHandlerTest {

    private static final String HEARING_ID = "2000000001";
    private static final String HEARING_DATE = "2023-01-20T10:00:00";
    private static final String HEARING_START_TIME = "10:00";
    private static final String HEARING_FINISH_TIME = "12:30";
    private static final String PART_HEARD_REASON_CODE = "BFA1-court-closure";
    private static final String CANCELLATION_REASON_CODE = "withdraw";

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HearingService hearingService;
    @Mock
    private HearingActualsService hearingActualsService;
    @Mock
    private UpdateHearingPayloadService updateHearingPayloadService;
    @Captor
    private ArgumentCaptor<HearingActuals> requestCaptor;

    private RecordAttendeesAndDurationRequestHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ATTENDEES_AND_DURATION);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID)).thenReturn(Optional.of(HEARING_ID));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_DATE, String.class)).thenReturn(Optional.of(HEARING_DATE));
        when(asylumCase.read(ATTENDEES_AND_DURATION_START_TIME, String.class))
            .thenReturn(Optional.of(HEARING_START_TIME));
        when(asylumCase.read(ATTENDEES_AND_DURATION_FINISH_TIME, String.class))
            .thenReturn(Optional.of(HEARING_FINISH_TIME));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_STAGE, String.class))
            .thenReturn(Optional.of(HearingType.SUBSTANTIVE.getLabel()));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class))
            .thenReturn(Optional.of(HearingResult.HEARD.getLabel()));

        handler = new RecordAttendeesAndDurationRequestHandler(
            hearingService,
            hearingActualsService,
            updateHearingPayloadService
        );
    }

    @Test
    void should_send_outcome_and_actual_days_as_hearing_actuals_then_complete() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(any(), any());
        InOrder inOrder = inOrder(hearingActualsService);
        inOrder.verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        inOrder.verify(hearingActualsService).completeHearingActuals(HEARING_ID);
        verifyNoMoreInteractions(hearingActualsService);
        verifyNoInteractions(hearingService);
        verifyNoInteractions(updateHearingPayloadService);

        HearingActuals hearingActuals = requestCaptor.getValue();
        HearingOutcome hearingOutcome = hearingActuals.getHearingOutcome();
        assertEquals("BFA1-SUB", hearingOutcome.getHearingType());
        assertEquals("COMPLETED", hearingOutcome.getHearingResult());
        assertFalse(hearingOutcome.isHearingFinalFlag());
        assertEquals("2023-01-20", hearingOutcome.getHearingResultDate());
        assertEquals(1, hearingActuals.getActualHearingDays().size());
        ActualHearingDay actualHearingDay = hearingActuals.getActualHearingDays().iterator().next();
        assertEquals("2023-01-20", actualHearingDay.getHearingDate());
        assertEquals("2023-01-20T10:00:00", actualHearingDay.getHearingStartTime());
        assertEquals("2023-01-20T12:30:00", actualHearingDay.getHearingEndTime());
        assertTrue(actualHearingDay.getPauseDateTimes().isEmpty());
        assertFalse(actualHearingDay.isNotRequired());
    }

    @Test
    void should_send_pause_when_paused_time_is_required() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_START, String.class)).thenReturn(Optional.of("10:25"));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_END, String.class)).thenReturn(Optional.of("10:35"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        ActualHearingDay actualHearingDay = requestCaptor.getValue().getActualHearingDays().iterator().next();
        assertEquals(List.of(new PauseDateTime("2023-01-20T10:25:00", "2023-01-20T10:35:00")),
            actualHearingDay.getPauseDateTimes());
    }

    @Test
    void should_not_send_pauses_when_paused_time_is_not_required() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_START, String.class)).thenReturn(Optional.of("10:25"));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_END, String.class)).thenReturn(Optional.of("10:35"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        ActualHearingDay actualHearingDay = requestCaptor.getValue().getActualHearingDays().iterator().next();
        assertTrue(actualHearingDay.getPauseDateTimes().isEmpty());
    }

    @Test
    void should_send_no_pauses_when_paused_time_is_required_but_none_entered() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_START, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_END, String.class)).thenReturn(Optional.empty());

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        ActualHearingDay actualHearingDay = requestCaptor.getValue().getActualHearingDays().iterator().next();
        assertTrue(actualHearingDay.getPauseDateTimes().isEmpty());
    }

    @Test
    void should_map_hearing_stage_and_result_labels_to_hmc_values() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_STAGE, String.class))
            .thenReturn(Optional.of(HearingType.CMR.getLabel()));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class))
            .thenReturn(Optional.of(HearingResult.PART_HEARD.getLabel()));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingOutcome hearingOutcome = requestCaptor.getValue().getHearingOutcome();
        assertEquals("BFA1-CMR", hearingOutcome.getHearingType());
        assertEquals("ADJOURNED", hearingOutcome.getHearingResult());
    }

    @Test
    void should_set_hearing_type_to_none_and_leave_result_null_when_stage_and_result_are_missing() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_STAGE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class)).thenReturn(Optional.empty());

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingOutcome hearingOutcome = requestCaptor.getValue().getHearingOutcome();
        assertEquals("None", hearingOutcome.getHearingType());
        assertNull(hearingOutcome.getHearingResult());
        assertNull(hearingOutcome.getHearingResultReasonType());
    }

    @Test
    void should_set_hearing_type_to_none_and_leave_result_null_when_stage_and_result_are_unknown() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_STAGE, String.class))
            .thenReturn(Optional.of("SUBSTANTIVE"));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class)).thenReturn(Optional.of("HEARD"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingOutcome hearingOutcome = requestCaptor.getValue().getHearingOutcome();
        assertEquals("None", hearingOutcome.getHearingType());
        assertNull(hearingOutcome.getHearingResult());
        assertNull(hearingOutcome.getHearingResultReasonType());
    }

    @Test
    void should_set_part_heard_reason_as_result_reason_type_when_part_heard() {
        givenHearingResult(HearingResult.PART_HEARD);
        givenReason(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, PART_HEARD_REASON_CODE);
        givenReason(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, CANCELLATION_REASON_CODE);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingOutcome hearingOutcome = requestCaptor.getValue().getHearingOutcome();
        assertEquals("ADJOURNED", hearingOutcome.getHearingResult());
        assertEquals(PART_HEARD_REASON_CODE, hearingOutcome.getHearingResultReasonType());
    }

    @Test
    void should_set_cancellation_reason_as_result_reason_type_when_withdrawn() {
        givenHearingResult(HearingResult.WITHDRAWN);
        givenReason(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, PART_HEARD_REASON_CODE);
        givenReason(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, CANCELLATION_REASON_CODE);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingOutcome hearingOutcome = requestCaptor.getValue().getHearingOutcome();
        assertEquals("CANCELLED", hearingOutcome.getHearingResult());
        assertEquals(CANCELLATION_REASON_CODE, hearingOutcome.getHearingResultReasonType());
    }

    @ParameterizedTest
    @EnumSource(value = HearingResult.class, names = {"PART_HEARD", "WITHDRAWN"})
    void should_leave_result_reason_type_null_when_reason_is_missing(HearingResult hearingResult) {
        givenHearingResult(hearingResult);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON)).thenReturn(Optional.empty());
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON)).thenReturn(Optional.empty());

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        assertNull(requestCaptor.getValue().getHearingOutcome().getHearingResultReasonType());
    }

    @Test
    void should_ignore_reasons_when_heard() {
        givenReason(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, PART_HEARD_REASON_CODE);
        givenReason(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, CANCELLATION_REASON_CODE);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        assertNull(requestCaptor.getValue().getHearingOutcome().getHearingResultReasonType());
    }

    @Test
    void should_use_empty_dates_and_times_when_hearing_date_is_missing() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingActuals hearingActuals = requestCaptor.getValue();
        assertEquals("", hearingActuals.getHearingOutcome().getHearingResultDate());
        ActualHearingDay actualHearingDay = hearingActuals.getActualHearingDays().iterator().next();
        assertEquals("", actualHearingDay.getHearingDate());
        assertEquals("", actualHearingDay.getHearingStartTime());
        assertEquals("", actualHearingDay.getHearingEndTime());
    }

    @Test
    void should_use_empty_start_and_end_times_when_times_are_missing() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_START_TIME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ATTENDEES_AND_DURATION_FINISH_TIME, String.class)).thenReturn(Optional.empty());

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        ActualHearingDay actualHearingDay = requestCaptor.getValue().getActualHearingDays().iterator().next();
        assertEquals("2023-01-20", actualHearingDay.getHearingDate());
        assertEquals("", actualHearingDay.getHearingStartTime());
        assertEquals("", actualHearingDay.getHearingEndTime());
    }

    @Test
    void should_accept_hours_only_times() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_START_TIME, String.class)).thenReturn(Optional.of("10"));
        when(asylumCase.read(ATTENDEES_AND_DURATION_FINISH_TIME, String.class)).thenReturn(Optional.of("13"));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_START, String.class)).thenReturn(Optional.of("11"));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_END, String.class)).thenReturn(Optional.of("11:30"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        ActualHearingDay actualHearingDay = requestCaptor.getValue().getActualHearingDays().iterator().next();
        assertEquals("2023-01-20T10:00:00", actualHearingDay.getHearingStartTime());
        assertEquals("2023-01-20T13:00:00", actualHearingDay.getHearingEndTime());
        assertEquals(List.of(new PauseDateTime("2023-01-20T11:00:00", "2023-01-20T11:30:00")),
            actualHearingDay.getPauseDateTimes());
    }

    @Test
    void should_accept_single_digit_hours() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_START_TIME, String.class)).thenReturn(Optional.of("9"));
        when(asylumCase.read(ATTENDEES_AND_DURATION_FINISH_TIME, String.class)).thenReturn(Optional.of("9:45"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        ActualHearingDay actualHearingDay = requestCaptor.getValue().getActualHearingDays().iterator().next();
        assertEquals("2023-01-20T09:00:00", actualHearingDay.getHearingStartTime());
        assertEquals("2023-01-20T09:45:00", actualHearingDay.getHearingEndTime());
    }

    @Test
    void should_not_update_hearing_actuals_when_no_hearing_id() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, callbackResponse.getData());
        verifyNoInteractions(hearingActualsService);
        verify(asylumCase, never()).write(any(), any());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_only_handle_callback_for_target_event_and_stage() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT && event == RECORD_ATTENDEES_AND_DURATION) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private void givenHearingResult(HearingResult hearingResult) {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class))
            .thenReturn(Optional.of(hearingResult.getLabel()));
    }

    private void givenReason(AsylumCaseFieldDefinition field, String code) {
        Value value = new Value(code, code + " label");
        DynamicList reason = new DynamicList(value, List.of(value));
        when(asylumCase.read(field)).thenReturn(Optional.of(reason));
        when(asylumCase.read(field, DynamicList.class)).thenReturn(Optional.of(reason));
    }
}
