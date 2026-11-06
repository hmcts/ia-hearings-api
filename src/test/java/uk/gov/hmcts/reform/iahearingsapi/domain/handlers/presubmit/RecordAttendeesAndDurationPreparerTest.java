package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAttendeesAndDurationPreparerTest {

    private static final long CASE_ID = 1234L;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HearingService hearingService;
    @Mock
    private HearingsGetResponse hearingsGetResponse;
    @Mock
    private CaseHearing substantiveHearing;
    @Mock
    private CaseHearing cancelledHearing;
    @Mock
    private CaseHearing costsHearing;

    private RecordAttendeesAndDurationPreparer preparer;

    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ATTENDEES_AND_DURATION);

        preparer = new RecordAttendeesAndDurationPreparer(hearingService);
    }

    @Test
    void should_write_attendees_and_duration_hearing_list_from_case_hearings() {
        mockHearing(substantiveHearing, "1", HearingType.SUBSTANTIVE.getKey(), "Substantive",
            HmcStatus.AWAITING_LISTING);
        mockHearing(cancelledHearing, "2", HearingType.SUBSTANTIVE.getKey(), "Substantive",
            HmcStatus.CANCELLATION_SUBMITTED);
        mockHearing(costsHearing, "3", HearingType.COSTS.getKey(), "Costs",
            HmcStatus.AWAITING_LISTING);
        when(hearingsGetResponse.getCaseHearings())
            .thenReturn(List.of(substantiveHearing, cancelledHearing, costsHearing));
        when(hearingService.getHearings(CASE_ID)).thenReturn(hearingsGetResponse);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = preparer.handle(ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(hearingService).getHearings(CASE_ID);

        ArgumentCaptor<DynamicList> captor = ArgumentCaptor.forClass(DynamicList.class);
        verify(asylumCase).write(eq(ATTENDEES_AND_DURATION_HEARING), captor.capture());
        DynamicList result = captor.getValue();
        assertEquals(1, result.getListItems().size());
        assertEquals("1", result.getListItems().getFirst().getCode());
        assertEquals("Substantive (Waiting to be listed)", result.getListItems().getFirst().getLabel());
    }

    @Test
    void should_write_empty_hearing_list_when_no_hearings_returned() {
        when(hearingService.getHearings(CASE_ID)).thenReturn(null);

        preparer.handle(ABOUT_TO_START, callback);

        ArgumentCaptor<DynamicList> captor = ArgumentCaptor.forClass(DynamicList.class);
        verify(asylumCase).write(eq(ATTENDEES_AND_DURATION_HEARING), captor.capture());
        assertTrue(captor.getValue().getListItems().isEmpty());
    }

    private void mockHearing(CaseHearing hearing, String requestId, String hearingType,
                             String hearingTypeDescription, HmcStatus hmcStatus) {
        when(hearing.getHearingRequestId()).thenReturn(requestId);
        when(hearing.getHearingType()).thenReturn(hearingType);
        when(hearing.getHearingTypeDescription()).thenReturn(hearingTypeDescription);
        when(hearing.getHmcStatus()).thenReturn(hmcStatus);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);

        assertThatThrownBy(() -> preparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_only_handle_callback_for_target_event_and_stage() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = preparer.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_START && event == RECORD_ATTENDEES_AND_DURATION) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> preparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> preparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
