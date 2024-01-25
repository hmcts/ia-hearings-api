package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.DECISION_AND_REASONS_STARTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DecisionAndReasonsStartedHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HearingService hearingService;

    private DecisionAndReasonsStartedHandler handler;


    @BeforeEach
    void setUp() {
        handler = new DecisionAndReasonsStartedHandler(hearingService);
    }

    @Test
    void should_handle_successfully() {
        when(callback.getEvent()).thenReturn(DECISION_AND_REASONS_STARTED);
        Assertions.assertTrue(handler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_fail_to_handle_if_invalid_event() {
        when(callback.getEvent()).thenReturn(END_APPEAL);
        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_send_hearing_creation_request_to_hmc() {
        when(callback.getEvent()).thenReturn(DECISION_AND_REASONS_STARTED);
        when(hearingService.createHearingWithPayload(callback)).thenReturn(asylumCase);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, times(1)).createHearingWithPayload(callback);
    }

    @Test
    void should_throw_exception_if_call_unsuccessful() {
        when(callback.getEvent()).thenReturn(DECISION_AND_REASONS_STARTED);
        when(hearingService.createHearingWithPayload(callback)).thenThrow(new IllegalStateException());

        assertThrows(IllegalStateException.class, () -> handler.handle(ABOUT_TO_SUBMIT, callback));

        verify(asylumCase, never()).write(MANUAL_CREATE_HEARINGS_REQUIRED, NO);
    }

}
