package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.bail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingParticipant;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.IndividualDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.OrganisationDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.Party;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartyOneOff;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordTheDecisionParticipantsMidEventHandlerTest {

    private static final String HEARING_ID = "2000001";
    private static final String FIRST_DAY = "2026-11-03T10:00:00";
    private static final String SECOND_DAY = "2026-11-04T10:00:00";

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private HearingActualsService hearingActualsService;

    private final BailCase bailCase = new BailCase();
    private RecordTheDecisionParticipantsMidEventHandler handler;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getPageId()).thenReturn(RecordTheDecisionParticipantsMidEventHandler.TIMES_PAGE_ID);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(actuals());

        bailCase.write(ATTENDEES_AND_DURATION_HEARING_ID, HEARING_ID);
        bailCase.write(ATTENDEES_AND_DURATION_DATE_OF_HEARING, selected(SECOND_DAY));

        handler = new RecordTheDecisionParticipantsMidEventHandler(hearingActualsService);
    }

    @Test
    void should_pre_fill_the_individuals_attending_the_chosen_hearing_day() {
        handler.handle(MID_EVENT, callback);

        List<HearingParticipant> participants = participants();
        assertEquals(2, participants.size());

        HearingParticipant applicant = participants.get(0);
        assertEquals("P1", applicant.getPartyId());
        assertEquals("José", applicant.getFirstNameLabel());
        assertEquals("González", applicant.getLastName());
        assertEquals("APEL", applicant.getRole().getValue().getCode());
        assertEquals("Appellant", applicant.getRoleLabel());
        assertEquals("VIDCVP", applicant.getAttendanceType().getValue().getCode());
        assertEquals(YesOrNo.YES, applicant.getPrefilled());

        HearingParticipant legalRep = participants.get(1);
        assertEquals("LGRP", legalRep.getRole().getValue().getCode());
        assertEquals("Legal Representative", legalRep.getRoleLabel());

        assertEquals(HEARING_ID + "|" + SECOND_DAY,
                     bailCase.read(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, String.class).orElse(""));
    }

    @Test
    void should_keep_the_recorded_participants_when_coming_back_for_the_same_hearing_day() {
        List<IdValue<HearingParticipant>> recorded = List.of(
            new IdValue<>("1", HearingParticipant.builder().firstName("Edited").build()));
        bailCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS, recorded);
        bailCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, HEARING_ID + "|" + SECOND_DAY);

        handler.handle(MID_EVENT, callback);

        assertEquals("Edited", participants().get(0).getFirstName());
        verifyNoInteractions(hearingActualsService);
    }

    @Test
    void should_pre_fill_again_when_another_hearing_day_is_chosen() {
        bailCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS, List.of(
            new IdValue<>("1", HearingParticipant.builder().firstName("Edited").build())));
        bailCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, HEARING_ID + "|" + FIRST_DAY);

        handler.handle(MID_EVENT, callback);

        List<HearingParticipant> participants = participants();
        assertEquals(2, participants.size());
        assertEquals("José", participants.get(0).getFirstName());
    }

    @Test
    void should_not_call_hmc_when_no_hearing_was_selected() {
        bailCase.clear(ATTENDEES_AND_DURATION_HEARING_ID);

        handler.handle(MID_EVENT, callback);

        assertTrue(bailCase.read(ATTENDEES_AND_DURATION_PARTICIPANTS).isEmpty());
        verifyNoInteractions(hearingActualsService);
    }

    @Test
    void should_only_handle_the_times_page_of_record_the_decision() {
        assertTrue(handler.canHandle(MID_EVENT, callback));
        assertFalse(handler.canHandle(ABOUT_TO_SUBMIT, callback));

        when(callback.getPageId()).thenReturn("selectHearingRecordAttendeesAndDuration");
        assertFalse(handler.canHandle(MID_EVENT, callback));

        when(callback.getPageId()).thenReturn(RecordTheDecisionParticipantsMidEventHandler.TIMES_PAGE_ID);
        when(callback.getEvent()).thenReturn(Event.RECORD_ATTENDEES_AND_DURATION);
        assertFalse(handler.canHandle(MID_EVENT, callback));

        assertThatThrownBy(() -> handler.handle(MID_EVENT, callback))
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> handler.canHandle(MID_EVENT, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private List<HearingParticipant> participants() {
        return bailCase.<List<IdValue<HearingParticipant>>>read(ATTENDEES_AND_DURATION_PARTICIPANTS)
            .orElse(List.of())
            .stream()
            .map(IdValue::getValue)
            .toList();
    }

    private static HearingActualsData actuals() {
        Party applicant = Party.builder()
            .partyId("P1")
            .partyRole("APPL")
            .partyType("IND")
            .partyChannelSubType("VIDCVP")
            .oneOf(List.of(PartyOneOff.builder()
                .individualDetails(IndividualDetails.builder().firstName("José").lastName("González").build())
                .build()))
            .build();
        Party legalRep = Party.builder()
            .partyId("P2")
            .partyRole("LGRP")
            .partyType("IND")
            .oneOf(List.of(PartyOneOff.builder()
                .individualDetails(IndividualDetails.builder().firstName("Stephen").lastName("Fenn").build())
                .build()))
            .build();
        Party homeOffice = Party.builder()
            .partyId("P3")
            .partyRole("RESP")
            .partyType("ORG")
            .oneOf(List.of(PartyOneOff.builder()
                .organisationDetails(OrganisationDetails.builder().name("Home Office").build())
                .build()))
            .build();

        return HearingActualsData.builder()
            .hearingPlanned(HearingPlanned.builder()
                .plannedHearingDays(List.of(
                    PlannedHearingDay.builder().plannedStartTime(FIRST_DAY).parties(List.of(applicant)).build(),
                    PlannedHearingDay.builder()
                        .plannedStartTime(SECOND_DAY)
                        .parties(List.of(applicant, legalRep, homeOffice))
                        .build()))
                .build())
            .build();
    }

    private static DynamicList selected(String code) {
        return new DynamicList(new Value(code, code), List.of(new Value(code, code)));
    }
}
