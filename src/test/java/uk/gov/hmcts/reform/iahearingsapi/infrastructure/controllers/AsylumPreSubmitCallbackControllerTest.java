package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.AsylumPreSubmitCallbackDispatcher;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsylumPreSubmitCallbackControllerTest {

    @Mock private AsylumPreSubmitCallbackDispatcher callbackDispatcher;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @InjectMocks
    private AsylumPreSubmitCallbackController asylumPreSubmitCallbackController;

    @Test
    void givenAboutToStartCallback_whenDispatched_thenResponseIsReturned() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback)).thenReturn(callbackResponse);

        Assertions.assertSame(callbackResponse, asylumPreSubmitCallbackController.ccdAboutToStart(callback).getBody());

        verify(callbackDispatcher).handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    @Test
    void givenAboutToSubmitCallback_whenDispatched_thenResponseIsReturned() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)).thenReturn(callbackResponse);

        Assertions.assertSame(callbackResponse, asylumPreSubmitCallbackController.ccdAboutToSubmit(callback).getBody());

        verify(callbackDispatcher).handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }

    @Test
    void should_not_allow_null_constructor_arguments() {
        assertThatThrownBy(() -> new AsylumPreSubmitCallbackController(null))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
