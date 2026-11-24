package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.bail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_CASE_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_METHODS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAPER_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordTheDecisionHearingSelectedMidEventHandlerTest {

    private static final long CASE_ID = 1234L;
    private static final String HEARING_ID = "2000001";
    private static final String PAGE_ID = "selectHearingRecordAttendeesAndDuration";
    private static final String DAY_ONE = "2026-11-02T10:00:00";
    private static final String DAY_TWO = "2026-11-03T10:00:00";

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private HearingService hearingService;
    @Mock
    private HearingActualsService hearingActualsService;
    @Mock
    private HearingGetResponse hearingGetResponse;
    @Mock
    private HearingDetails hearingDetails;

    private final BailCase bailCase = new BailCase();
    private RecordTheDecisionHearingSelectedMidEventHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getPageId()).thenReturn(PAGE_ID);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(hearingService.getHearing(HEARING_ID)).thenReturn(hearingGetResponse);
        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        when(hearingDetails.getHearingChannels()).thenReturn(List.of("INTER", "VID"));
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(actuals(DAY_ONE, DAY_TWO));

        bailCase.write(ATTENDEES_AND_DURATION_HEARING, selected(HEARING_ID));

        handler = new RecordTheDecisionHearingSelectedMidEventHandler(hearingService, hearingActualsService);
    }

    @Test
    void should_write_the_selected_hearing_details() {
        handler.handle(MID_EVENT, callback);

        assertEquals("1234", bailCase.read(ATTENDEES_AND_DURATION_CASE_REFERENCE, String.class).orElseThrow());
        assertEquals(HEARING_ID, bailCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class).orElseThrow());
        assertEquals(DAY_ONE, bailCase.read(ATTENDEES_AND_DURATION_HEARING_DATE, String.class).orElseThrow());
        assertEquals(
            "2 November 2026, 10:00",
            bailCase.read(ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY, String.class).orElseThrow()
        );
        assertEquals("No", bailCase.read(ATTENDEES_AND_DURATION_PAPER_HEARING, String.class).orElseThrow());
        assertEquals(
            "In Person, Video",
            bailCase.read(ATTENDEES_AND_DURATION_HEARING_METHODS, String.class).orElseThrow()
        );

        DynamicList dates = bailCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class).orElseThrow();
        assertEquals(List.of(DAY_ONE, DAY_TWO), dates.getListItems().stream().map(Value::getCode).toList());
        assertEquals("", dates.getValue().getCode());
    }

    @Test
    void should_mark_a_paper_hearing_and_keep_unknown_channels_as_they_are() {
        when(hearingDetails.getHearingChannels()).thenReturn(List.of("ONPPRS", "OTHER"));

        handler.handle(MID_EVENT, callback);

        assertEquals("Yes", bailCase.read(ATTENDEES_AND_DURATION_PAPER_HEARING, String.class).orElseThrow());
        assertEquals(
            "On the Papers, OTHER",
            bailCase.read(ATTENDEES_AND_DURATION_HEARING_METHODS, String.class).orElseThrow()
        );
    }

    @Test
    void should_keep_the_hearing_day_already_chosen() {
        handler.handle(MID_EVENT, callback);
        DynamicList dates = bailCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class).orElseThrow();
        bailCase.write(
            ATTENDEES_AND_DURATION_DATE_OF_HEARING,
            new DynamicList(dates.getListItems().get(1), dates.getListItems())
        );

        handler.handle(MID_EVENT, callback);

        assertEquals(
            DAY_TWO,
            bailCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class).orElseThrow().getValue().getCode()
        );
    }

    @Test
    void should_cope_with_a_hearing_that_has_no_planned_days_or_channels() {
        when(hearingDetails.getHearingChannels()).thenReturn(null);
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(new HearingActualsData());

        handler.handle(MID_EVENT, callback);

        assertEquals("", bailCase.read(ATTENDEES_AND_DURATION_HEARING_DATE, String.class).orElseThrow());
        assertEquals("", bailCase.read(ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY, String.class).orElseThrow());
        assertEquals("", bailCase.read(ATTENDEES_AND_DURATION_HEARING_METHODS, String.class).orElseThrow());
        assertTrue(bailCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class)
            .orElseThrow().getListItems().isEmpty());
    }

    @Test
    void should_do_nothing_when_no_hearing_is_selected() {
        bailCase.write(ATTENDEES_AND_DURATION_HEARING, selected(""));

        handler.handle(MID_EVENT, callback);

        verifyNoInteractions(hearingService, hearingActualsService);
        assertTrue(bailCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class).isEmpty());
    }

    @Test
    void should_only_handle_the_select_hearing_page_of_record_the_decision() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                boolean expected = event == Event.RECORD_THE_DECISION && stage == MID_EVENT;
                assertEquals(expected, handler.canHandle(stage, callback));
            }
        }

        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getPageId()).thenReturn("tribunalDecision");
        assertFalse(handler.canHandle(MID_EVENT, callback));
        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> handler.canHandle(MID_EVENT, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private static DynamicList selected(String code) {
        return new DynamicList(new Value(code, code), List.of(new Value(HEARING_ID, "Bail")));
    }

    private static HearingActualsData actuals(String... plannedStartTimes) {
        List<PlannedHearingDay> days = List.of(plannedStartTimes).stream()
            .map(start -> PlannedHearingDay.builder().plannedStartTime(start).build())
            .toList();
        return HearingActualsData.builder()
            .hearingPlanned(HearingPlanned.builder().plannedHearingDays(days).build())
            .build();
    }
}
