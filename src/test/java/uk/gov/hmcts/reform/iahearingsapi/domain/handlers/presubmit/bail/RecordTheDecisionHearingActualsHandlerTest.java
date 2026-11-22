package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.bail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_END;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_START;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_START_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingParticipant;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualDayParty;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActuals;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PauseDateTime;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordTheDecisionHearingActualsHandlerTest {

    private static final String HEARING_ID = "2000001";

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private HearingActualsService hearingActualsService;

    private final BailCase bailCase = new BailCase();
    private RecordTheDecisionHearingActualsHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(caseDetails.getId()).thenReturn(1234L);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        bailCase.write(ATTENDEES_AND_DURATION_HEARING_ID, HEARING_ID);
        bailCase.write(ATTENDEES_AND_DURATION_DATE_OF_HEARING, selected("2026-11-03T10:00:00"));
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_STAGE, "BFA1-BAI");
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_RESULT, "COMPLETED");
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED, YesOrNo.YES);
        bailCase.write(ATTENDEES_AND_DURATION_START_TIME, "10:15");
        bailCase.write(ATTENDEES_AND_DURATION_FINISH_TIME, "12");
        bailCase.write(ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED, YesOrNo.NO);

        handler = new RecordTheDecisionHearingActualsHandler(hearingActualsService);
    }

    @Test
    void should_send_then_complete_the_hearing_actuals() {
        handler.handle(ABOUT_TO_SUBMIT, callback);

        InOrder inOrder = inOrder(hearingActualsService);
        ArgumentCaptor<HearingActuals> captor = ArgumentCaptor.forClass(HearingActuals.class);
        inOrder.verify(hearingActualsService).updateHearingActuals(captor.capture(), eq(HEARING_ID));
        inOrder.verify(hearingActualsService).completeHearingActuals(HEARING_ID);

        HearingActuals actuals = captor.getValue();
        assertEquals("BFA1-BAI", actuals.getHearingOutcome().getHearingType());
        assertEquals("COMPLETED", actuals.getHearingOutcome().getHearingResult());
        assertEquals("2026-11-03", actuals.getHearingOutcome().getHearingResultDate());
        assertNull(actuals.getHearingOutcome().getHearingResultReasonType());
        assertFalse(actuals.getHearingOutcome().isHearingFinalFlag());

        ActualHearingDay day = onlyDay(actuals);
        assertEquals("2026-11-03", day.getHearingDate());
        assertEquals("2026-11-03T10:15:00", day.getHearingStartTime());
        assertEquals("2026-11-03T12:00:00", day.getHearingEndTime());
        assertTrue(day.getPauseDateTimes().isEmpty());
        assertTrue(day.getActualDayParties().isEmpty());
        assertFalse(day.isNotRequired());
    }

    @Test
    void should_send_the_paused_time_when_required() {
        bailCase.write(ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED, YesOrNo.YES);
        bailCase.write(ATTENDEES_AND_DURATION_PAUSE_START, "11:00");
        bailCase.write(ATTENDEES_AND_DURATION_PAUSE_END, "11:20");

        handler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(
            List.of(new PauseDateTime("2026-11-03T11:00:00", "2026-11-03T11:20:00")),
            onlyDay(sentActuals()).getPauseDateTimes()
        );
    }

    @Test
    void should_not_send_times_when_the_hearing_day_was_not_required() {
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED, YesOrNo.NO);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        ActualHearingDay day = onlyDay(sentActuals());
        assertTrue(day.isNotRequired());
        assertNull(day.getHearingStartTime());
        assertNull(day.getHearingEndTime());
        assertTrue(day.getPauseDateTimes().isEmpty());
    }

    @Test
    void should_send_the_recorded_participants() {
        bailCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS, List.of(
            new IdValue<>("1", HearingParticipant.builder()
                .partyId("P1")
                .firstName("José")
                .lastName("González")
                .role(selected("APEL"))
                .attendanceType(selected("VIDCVP"))
                .prefilled(YesOrNo.YES)
                .build()),
            new IdValue<>("2", HearingParticipant.builder()
                .firstName("Ann")
                .lastName("Smith")
                .role(selected("LGRP"))
                .representedParty(selected("APEL"))
                .attendanceType(selected("NA"))
                .build())
        ));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        List<ActualDayParty> parties = onlyDay(sentActuals()).getActualDayParties();
        assertEquals(2, parties.size());

        ActualDayParty applicant = parties.get(0);
        assertEquals("P1", applicant.getActualPartyId());
        assertEquals("APEL", applicant.getPartyRole());
        assertEquals("José", applicant.getIndividualDetails().getFirstName());
        assertEquals("González", applicant.getIndividualDetails().getLastName());
        assertEquals("VIDCVP", applicant.getPartyChannelSubType());
        assertFalse(applicant.getDidNotAttendFlag());
        assertNull(applicant.getRepresentedParty());

        ActualDayParty legalRep = parties.get(1);
        assertEquals("LGRP", legalRep.getPartyRole());
        assertEquals("NA", legalRep.getPartyChannelSubType());
        assertTrue(legalRep.getDidNotAttendFlag());
        assertEquals("P1", legalRep.getRepresentedParty());
    }

    @Test
    void should_not_send_participants_when_the_hearing_day_was_not_required() {
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED, YesOrNo.NO);
        bailCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS, List.of(
            new IdValue<>("1", HearingParticipant.builder().partyId("P1").role(selected("APEL")).build())
        ));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        assertTrue(onlyDay(sentActuals()).getActualDayParties().isEmpty());
    }

    @Test
    void should_send_the_part_heard_reason_for_an_adjourned_hearing() {
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_RESULT, "ADJOURNED");
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, selected("partHeardReason"));
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, selected("cancellationReason"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals("ADJOURNED", sentActuals().getHearingOutcome().getHearingResult());
        assertEquals("partHeardReason", sentActuals().getHearingOutcome().getHearingResultReasonType());
    }

    @Test
    void should_send_the_cancellation_reason_for_a_cancelled_hearing() {
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_RESULT, "CANCELLED");
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON, selected("partHeardReason"));
        bailCase.write(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON, selected("cancellationReason"));

        handler.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals("CANCELLED", sentActuals().getHearingOutcome().getHearingResult());
        assertEquals("cancellationReason", sentActuals().getHearingOutcome().getHearingResultReasonType());
    }

    @Test
    void should_use_the_selected_hearing_and_first_planned_day_when_not_sent_back() {
        bailCase.clear(ATTENDEES_AND_DURATION_HEARING_ID);
        bailCase.clear(ATTENDEES_AND_DURATION_DATE_OF_HEARING);
        bailCase.clear(ATTENDEES_AND_DURATION_HEARING_STAGE);
        bailCase.write(ATTENDEES_AND_DURATION_HEARING, selected(HEARING_ID));
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(
            HearingActualsData.builder()
                .hearingPlanned(HearingPlanned.builder()
                    .plannedHearingDays(List.of(
                        PlannedHearingDay.builder().plannedStartTime("2026-11-02T09:30:00").build()))
                    .build())
                .build());

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingActualsService).updateHearingActuals(any(HearingActuals.class), eq(HEARING_ID));
        assertEquals("2026-11-02", onlyDay(sentActuals()).getHearingDate());
        assertEquals("BFA1-BAI", sentActuals().getHearingOutcome().getHearingType());
    }

    @Test
    void should_not_call_hmc_when_no_hearing_was_selected() {
        bailCase.clear(ATTENDEES_AND_DURATION_HEARING_ID);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verifyNoInteractions(hearingActualsService);
    }

    @Test
    void should_not_complete_the_actuals_when_the_update_fails() {
        doThrow(new HmcException(new RuntimeException("failed")))
            .when(hearingActualsService).updateHearingActuals(any(HearingActuals.class), eq(HEARING_ID));

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback)).isInstanceOf(HmcException.class);

        verify(hearingActualsService, never()).completeHearingActuals(any());
    }

    @Test
    void should_only_handle_about_to_submit_of_record_the_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean expected = event == Event.RECORD_THE_DECISION && stage == ABOUT_TO_SUBMIT;
                assertEquals(expected, handler.canHandle(stage, callback));
            }
        }

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> handler.canHandle(ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private HearingActuals sentActuals() {
        ArgumentCaptor<HearingActuals> captor = ArgumentCaptor.forClass(HearingActuals.class);
        verify(hearingActualsService).updateHearingActuals(captor.capture(), eq(HEARING_ID));
        return captor.getValue();
    }

    private static ActualHearingDay onlyDay(HearingActuals actuals) {
        assertEquals(1, actuals.getActualHearingDays().size());
        return actuals.getActualHearingDays().iterator().next();
    }

    private static DynamicList selected(String code) {
        return new DynamicList(new Value(code, code), List.of(new Value(code, code)));
    }
}
