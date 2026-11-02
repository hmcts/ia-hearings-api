package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_START_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActuals;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingOutcome;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingResult;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAttendeesAndDurationRequestHandlerTest {

    private static final String HEARING_ID = "2000000001";
    private static final String HEARING_DATE = "2023-01-20";
    private static final String HEARING_START_TIME = "10:00";
    private static final String HEARING_FINISH_TIME = "12:30";

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
    private ArgumentCaptor<HearingActualsData> requestCaptor;

    private final HearingPlanned hearingPlanned = HearingPlanned.builder().plannedHearingType("SUBSTANTIVE").build();
    private HearingActualsData existingHearingActuals;

    private RecordAttendeesAndDurationRequestHandler handler;

    @BeforeEach
    void setUp() {
        existingHearingActuals = HearingActualsData.builder().hearingPlanned(hearingPlanned).build();
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ATTENDEES_AND_DURATION);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID)).thenReturn(Optional.of(HEARING_ID));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_DATE, String.class)).thenReturn(Optional.of(HEARING_DATE));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_START_TIME, String.class))
            .thenReturn(Optional.of(HEARING_START_TIME));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_FINISH_TIME, String.class))
            .thenReturn(Optional.of(HEARING_FINISH_TIME));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_STAGE, String.class))
            .thenReturn(Optional.of(HearingType.SUBSTANTIVE.getLabel()));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class))
            .thenReturn(Optional.of(HearingResult.HEARD.getLabel()));
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(existingHearingActuals);

        handler = new RecordAttendeesAndDurationRequestHandler(
            hearingService,
            hearingActualsService,
            updateHearingPayloadService
        );
    }

    @Test
    void should_add_outcome_and_actual_days_to_existing_hearing_actuals_then_update_and_complete() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = handler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(asylumCase, never()).write(any(), any());
        InOrder inOrder = inOrder(hearingActualsService);
        inOrder.verify(hearingActualsService).getHearingActuals(HEARING_ID);
        inOrder.verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        inOrder.verify(hearingActualsService).completeHearingActuals(HEARING_ID);
        verifyNoMoreInteractions(hearingActualsService);
        verifyNoInteractions(hearingService);
        verifyNoInteractions(updateHearingPayloadService);

        HearingActualsData request = requestCaptor.getValue();
        assertSame(existingHearingActuals, request);
        assertSame(hearingPlanned, request.getHearingPlanned());
        HearingActuals hearingActuals = request.getHearingActuals();
        HearingOutcome hearingOutcome = hearingActuals.getHearingOutcome();
        assertEquals("BFA1-SUB", hearingOutcome.getHearingType());
        assertEquals("HEARD", hearingOutcome.getHearingResult());
        assertEquals(HEARING_DATE, hearingOutcome.getHearingResultDate());
        assertEquals(1, hearingActuals.getActualHearingDays().size());
        ActualHearingDay actualHearingDay = hearingActuals.getActualHearingDays().iterator().next();
        assertEquals(HEARING_DATE, actualHearingDay.getHearingDate());
        assertEquals(HEARING_START_TIME, actualHearingDay.getHearingStartTime());
        assertEquals(HEARING_FINISH_TIME, actualHearingDay.getHearingEndTime());
    }

    @Test
    void should_map_hearing_stage_and_result_labels_to_hmc_values() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_STAGE, String.class))
            .thenReturn(Optional.of(HearingType.CMR.getLabel()));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class))
            .thenReturn(Optional.of(HearingResult.PART_HEARD.getLabel()));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingOutcome hearingOutcome = requestCaptor.getValue().getHearingActuals().getHearingOutcome();
        assertEquals("BFA1-CMR", hearingOutcome.getHearingType());
        assertEquals("PART_HEARD", hearingOutcome.getHearingResult());
    }

    @Test
    void should_set_hearing_type_and_result_to_none_when_stage_and_result_are_missing() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_STAGE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class)).thenReturn(Optional.empty());

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingOutcome hearingOutcome = requestCaptor.getValue().getHearingActuals().getHearingOutcome();
        assertEquals("None", hearingOutcome.getHearingType());
        assertEquals("None", hearingOutcome.getHearingResult());
    }

    @Test
    void should_set_hearing_type_and_result_to_none_when_stage_and_result_are_unknown() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_STAGE, String.class))
            .thenReturn(Optional.of("SUBSTANTIVE"));
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class)).thenReturn(Optional.of("HEARD"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingOutcome hearingOutcome = requestCaptor.getValue().getHearingActuals().getHearingOutcome();
        assertEquals("None", hearingOutcome.getHearingType());
        assertEquals("None", hearingOutcome.getHearingResult());
    }

    @Test
    void should_use_empty_values_when_hearing_date_and_times_are_missing() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_START_TIME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_FINISH_TIME, String.class)).thenReturn(Optional.empty());

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(requestCaptor.capture(), eq(HEARING_ID));
        HearingActuals hearingActuals = requestCaptor.getValue().getHearingActuals();
        assertEquals("", hearingActuals.getHearingOutcome().getHearingResultDate());
        ActualHearingDay actualHearingDay = hearingActuals.getActualHearingDays().iterator().next();
        assertEquals("", actualHearingDay.getHearingDate());
        assertEquals("", actualHearingDay.getHearingStartTime());
        assertEquals("", actualHearingDay.getHearingEndTime());
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
}
