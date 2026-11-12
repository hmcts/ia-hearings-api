package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_JUDGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_METHODS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_START_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAPER_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;

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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingParticipant;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.IndividualDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.OrganisationDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.Party;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartyOneOff;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAttendeesAndDurationMidEventHandlerTest {

    private static final String SELECT_HEARING_PAGE_ID = "selectHearingRecordAttendeesAndDuration";
    private static final String OTHER_PAGE_ID = "recordAttendeesAndDuration";
    private static final String TIMES_PAGE_ID = "timesRecordAttendeesAndDuration";
    private static final String HEARING_ID = "2000000001";
    private static final String PLANNED_START_TIME = "2023-01-20T10:00:00";
    private static final String SECOND_PLANNED_START_TIME = "2023-01-21T10:00:00";

    private static final List<AsylumCaseFieldDefinition> HEARING_FIELDS = List.of(
        ATTENDEES_AND_DURATION_HEARING_ID,
        ATTENDEES_AND_DURATION_HEARING_DATE,
        ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY,
        ATTENDEES_AND_DURATION_PAPER_HEARING,
        ATTENDEES_AND_DURATION_HEARING_METHODS,
        ATTENDEES_AND_DURATION_HEARING_STAGE,
        ATTENDEES_AND_DURATION_HEARING_RESULT,
        ATTENDEES_AND_DURATION_HEARING_JUDGE,
        ATTENDEES_AND_DURATION_DATE_OF_HEARING,
        ATTENDEES_AND_DURATION_START_TIME,
        ATTENDEES_AND_DURATION_FINISH_TIME
    );

    private static final List<AsylumCaseFieldDefinition> BLANKED_FIELDS = List.of(
        ATTENDEES_AND_DURATION_HEARING_STAGE,
        ATTENDEES_AND_DURATION_HEARING_RESULT,
        ATTENDEES_AND_DURATION_HEARING_JUDGE,
        ATTENDEES_AND_DURATION_START_TIME,
        ATTENDEES_AND_DURATION_FINISH_TIME
    );

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
    private HearingGetResponse hearingGetResponse;
    @Mock
    private HearingDetails hearingDetails;

    private RecordAttendeesAndDurationMidEventHandler handler;

    @BeforeEach
    void setUp() {
        Value selectedHearing = new Value(HEARING_ID, "Substantive (Listed) - 20 January 2023");
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ATTENDEES_AND_DURATION);
        when(callback.getPageId()).thenReturn(SELECT_HEARING_PAGE_ID);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(selectedHearing, List.of(selectedHearing))));
        when(hearingService.getHearing(HEARING_ID)).thenReturn(hearingGetResponse);
        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        when(hearingDetails.getHearingChannels()).thenReturn(List.of("INTER", "VID"));
        PlannedHearingDay plannedHearingDay = PlannedHearingDay.builder().plannedStartTime(PLANNED_START_TIME).build();
        when(hearingActualsService.getHearingActuals(HEARING_ID))
            .thenReturn(hearingActuals(List.of(plannedHearingDay)));

        handler = new RecordAttendeesAndDurationMidEventHandler(hearingService, hearingActualsService);
    }

    @Test
    void should_fetch_selected_hearing_and_write_its_details() {
        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(MID_EVENT, callback);

        assertEquals(asylumCase, response.getData());
        verify(hearingService).getHearing(HEARING_ID);
        verify(hearingActualsService).getHearingActuals(HEARING_ID);
        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_ID, HEARING_ID);
        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_DATE, PLANNED_START_TIME);
        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY, "20 January 2023, 10:00");
        verify(asylumCase).write(ATTENDEES_AND_DURATION_PAPER_HEARING, YesOrNo.NO.toString());
        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_METHODS, "In Person, Video");
        BLANKED_FIELDS.forEach(field -> verify(asylumCase).write(field, ""));
        verify(asylumCase).write(
            ATTENDEES_AND_DURATION_DATE_OF_HEARING,
            new DynamicList(new Value("", ""), List.of(new Value(PLANNED_START_TIME, "20 January 2023")))
        );
    }

    @Test
    void should_write_all_planned_hearing_days_as_dates_of_hearing() {
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(hearingActuals(List.of(
            PlannedHearingDay.builder().plannedStartTime(PLANNED_START_TIME).build(),
            PlannedHearingDay.builder().plannedStartTime(SECOND_PLANNED_START_TIME).build()
        )));

        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_DATE, PLANNED_START_TIME);
        verify(asylumCase).write(
            ATTENDEES_AND_DURATION_DATE_OF_HEARING,
            new DynamicList(new Value("", ""), List.of(
                new Value(PLANNED_START_TIME, "20 January 2023"),
                new Value(SECOND_PLANNED_START_TIME, "21 January 2023")
            ))
        );
    }

    @Test
    void should_label_dates_of_hearing_in_uk_time() {
        String lateEveningUtcInSummer = "2023-06-20T23:30:00";
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(hearingActuals(List.of(
            PlannedHearingDay.builder().plannedStartTime(lateEveningUtcInSummer).build()
        )));

        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(
            ATTENDEES_AND_DURATION_DATE_OF_HEARING,
            new DynamicList(new Value("", ""), List.of(new Value(lateEveningUtcInSummer, "21 June 2023")))
        );
        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY, "21 June 2023, 00:30");
    }

    @Test
    void should_write_hearing_methods_as_comma_separated_labels() {
        when(hearingDetails.getHearingChannels()).thenReturn(List.of("TEL", "NA", "ONPPRS"));

        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_METHODS, "Telephone, Not in Attendance, On the Papers");
    }

    @Test
    void should_fail_for_unknown_hearing_method() {
        when(hearingDetails.getHearingChannels()).thenReturn(List.of("INTER", "UNKNOWN"));

        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_write_paper_hearing_true_when_hearing_is_on_the_papers() {
        when(hearingDetails.getHearingChannels()).thenReturn(List.of("ONPPRS"));

        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(ATTENDEES_AND_DURATION_PAPER_HEARING, YesOrNo.YES.toString());
    }

    @Test
    void should_write_empty_hearing_date_and_dates_of_hearing_when_no_planned_hearing_days() {
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(hearingActuals(List.of()));

        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_DATE, "");
        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY, "");
        verify(asylumCase).write(
            ATTENDEES_AND_DURATION_DATE_OF_HEARING,
            new DynamicList(new Value("", ""), List.of())
        );
    }

    @Test
    void should_not_write_hearing_details_when_no_hearing_selected() {
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class)).thenReturn(Optional.empty());

        handler.handle(MID_EVENT, callback);

        verifyNoInteractions(hearingService);
        verifyNoInteractions(hearingActualsService);
        HEARING_FIELDS.forEach(field -> verify(asylumCase, never()).write(eq(field), any()));
    }

    @Test
    void should_not_run_when_not_on_select_hearing_page() {
        when(callback.getPageId()).thenReturn(OTHER_PAGE_ID);

        handler.handle(MID_EVENT, callback);

        verifyNoInteractions(hearingService);
        verifyNoInteractions(hearingActualsService);
        HEARING_FIELDS.forEach(field -> verify(asylumCase, never()).write(eq(field), any()));
    }

    @Test
    void should_populate_participants_from_selected_hearing_day_on_times_page() {
        when(callback.getPageId()).thenReturn(TIMES_PAGE_ID);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_ID));
        Value selectedDay = new Value(SECOND_PLANNED_START_TIME, "21 January 2023");
        when(asylumCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(selectedDay, List.of(selectedDay))));
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(hearingActuals(List.of(
            PlannedHearingDay.builder().plannedStartTime(PLANNED_START_TIME)
                .parties(List.of(individual("p0", "WITN", "TEL", "Not", "Selected"))).build(),
            PlannedHearingDay.builder().plannedStartTime(SECOND_PLANNED_START_TIME).parties(List.of(
                individual("p1", "APEL", "VIDTEAMS", "John", "Smith"),
                individual("p2", "LGRP", "INTER", "Jane", "Doe"),
                individual("p3", "INTP", "TELCVP", "Ivan", "Petrov"),
                Party.builder().partyId("p4").partyRole("RESP").partyChannelSubType("NA")
                    .oneOf(List.of(PartyOneOff.builder()
                        .organisationDetails(OrganisationDetails.builder().name("Home Office").build())
                        .build()))
                    .build()
            )).build()
        )));

        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(ATTENDEES_AND_DURATION_PARTICIPANTS, List.of(
            new IdValue<>("1", new HearingParticipant("p1", "John", "Smith", "APEL", "VID", null)),
            new IdValue<>("2", new HearingParticipant("p2", "Jane", "Doe", "LR", "INTER", null)),
            new IdValue<>("3", new HearingParticipant("p3", "Ivan", "Petrov", null, "TEL", null)),
            new IdValue<>("4", new HearingParticipant("p4", null, null, "RESP", "NA", "Home Office"))
        ));
        verify(asylumCase).write(
            ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, HEARING_ID + "|" + SECOND_PLANNED_START_TIME);
        verifyNoInteractions(hearingService);
        HEARING_FIELDS.forEach(field -> verify(asylumCase, never()).write(eq(field), any()));
    }

    @Test
    void should_keep_participants_when_already_populated_for_same_hearing_day() {
        when(callback.getPageId()).thenReturn(TIMES_PAGE_ID);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_ID));
        Value selectedDay = new Value(PLANNED_START_TIME, "20 January 2023");
        when(asylumCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(selectedDay, List.of(selectedDay))));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, String.class))
            .thenReturn(Optional.of(HEARING_ID + "|" + PLANNED_START_TIME));

        handler.handle(MID_EVENT, callback);

        verifyNoInteractions(hearingActualsService);
        verify(asylumCase, never()).write(eq(ATTENDEES_AND_DURATION_PARTICIPANTS), any());
        verify(asylumCase, never()).write(eq(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE), any());
    }

    @Test
    void should_repopulate_participants_when_selected_hearing_day_changed() {
        when(callback.getPageId()).thenReturn(TIMES_PAGE_ID);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_ID));
        Value selectedDay = new Value(SECOND_PLANNED_START_TIME, "21 January 2023");
        when(asylumCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(selectedDay, List.of(selectedDay))));
        when(asylumCase.read(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, String.class))
            .thenReturn(Optional.of(HEARING_ID + "|" + PLANNED_START_TIME));
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(hearingActuals(List.of(
            PlannedHearingDay.builder().plannedStartTime(SECOND_PLANNED_START_TIME)
                .parties(List.of(individual("p1", "RESP", "INTER", "Ann", "Lee"))).build()
        )));

        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(ATTENDEES_AND_DURATION_PARTICIPANTS, List.of(
            new IdValue<>("1", new HearingParticipant("p1", "Ann", "Lee", "RESP", "INTER", null))
        ));
        verify(asylumCase).write(
            ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, HEARING_ID + "|" + SECOND_PLANNED_START_TIME);
    }

    @Test
    void should_reset_participants_source_on_select_hearing_page() {
        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, "");
    }

    @Test
    void should_populate_participants_from_first_hearing_day_when_no_day_selected() {
        when(callback.getPageId()).thenReturn(TIMES_PAGE_ID);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_ID));
        when(asylumCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class)).thenReturn(Optional.empty());
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(hearingActuals(List.of(
            PlannedHearingDay.builder().plannedStartTime(PLANNED_START_TIME)
                .parties(List.of(individual("p1", "RESP", "ONPPRS", "Ann", "Lee"))).build()
        )));

        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(ATTENDEES_AND_DURATION_PARTICIPANTS, List.of(
            new IdValue<>("1", new HearingParticipant("p1", "Ann", "Lee", "RESP", "ONPPRS", null))
        ));
    }

    @Test
    void should_write_empty_participants_when_no_planned_hearing_days() {
        when(callback.getPageId()).thenReturn(TIMES_PAGE_ID);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_ID));
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(hearingActuals(List.of()));

        handler.handle(MID_EVENT, callback);

        verify(asylumCase).write(ATTENDEES_AND_DURATION_PARTICIPANTS, List.of());
    }

    @Test
    void should_not_populate_participants_when_no_hearing_id() {
        when(callback.getPageId()).thenReturn(TIMES_PAGE_ID);
        when(asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)).thenReturn(Optional.empty());

        handler.handle(MID_EVENT, callback);

        verifyNoInteractions(hearingActualsService);
        verify(asylumCase, never()).write(eq(ATTENDEES_AND_DURATION_PARTICIPANTS), any());
    }

    @Test
    void should_fail_to_handle_if_invalid_event() {
        when(callback.getEvent()).thenReturn(END_APPEAL);

        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_only_handle_callback_for_target_event_and_stage() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = handler.canHandle(callbackStage, callback);

                if (callbackStage == MID_EVENT && event == RECORD_ATTENDEES_AND_DURATION) {
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

        assertThatThrownBy(() -> handler.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private static Party individual(String partyId, String role, String channel, String firstName, String lastName) {
        return Party.builder()
            .partyId(partyId)
            .partyRole(role)
            .partyChannelSubType(channel)
            .oneOf(List.of(PartyOneOff.builder()
                .individualDetails(IndividualDetails.builder().firstName(firstName).lastName(lastName).build())
                .build()))
            .build();
    }

    private static HearingActualsData hearingActuals(List<PlannedHearingDay> plannedHearingDays) {
        return HearingActualsData.builder()
            .hearingPlanned(HearingPlanned.builder().plannedHearingDays(plannedHearingDays).build())
            .build();
    }
}
