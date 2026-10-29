package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_METHODS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAPER_HEARING;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAttendeesAndDurationMidEventHandlerTest {

    private static final String SELECT_HEARING_PAGE_ID = "selectHearingRecordAttendeesAndDuration";
    private static final String OTHER_PAGE_ID = "recordAttendeesAndDuration";
    private static final String HEARING_ID = "2000000001";

    private static final List<AsylumCaseFieldDefinition> HEARING_FIELDS = List.of(
        ATTENDEES_AND_DURATION_HEARING_ID,
        ATTENDEES_AND_DURATION_HEARING_DATE,
        ATTENDEES_AND_DURATION_PAPER_HEARING,
        ATTENDEES_AND_DURATION_HEARING_METHODS
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
    private HearingActualsGetResponse hearingActualsGetResponse;
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
        when(hearingActualsService.getHearingActuals(HEARING_ID)).thenReturn(hearingActualsGetResponse);

        handler = new RecordAttendeesAndDurationMidEventHandler(hearingService, hearingActualsService);
    }

    @Test
    void should_fetch_selected_hearing_and_write_its_details() {
        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(MID_EVENT, callback);

        assertEquals(asylumCase, response.getData());
        verify(hearingService).getHearing(HEARING_ID);
        verify(hearingActualsService).getHearingActuals(HEARING_ID);
        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_ID, HEARING_ID);
        verify(asylumCase).write(eq(ATTENDEES_AND_DURATION_HEARING_DATE), anyString());
        verify(asylumCase).write(eq(ATTENDEES_AND_DURATION_PAPER_HEARING), anyString());
        verify(asylumCase).write(ATTENDEES_AND_DURATION_HEARING_METHODS, "[INTER, VID]");
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
}
