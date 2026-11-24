package uk.gov.hmcts.reform.iahearingsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.security.CcdEventAuthorizor;

@ExtendWith(MockitoExtension.class)
class BailPreSubmitCallbackDispatcherTest {

    @Mock
    private CcdEventAuthorizor ccdEventAuthorizor;
    @Mock
    private PreSubmitCallbackHandler<BailCase> handler;
    @Mock
    private PreSubmitCallbackHandler<BailCase> otherHandler;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;

    @Test
    void should_authorise_the_event_and_dispatch_to_the_handlers_that_can_handle_it() {
        BailCase updatedCase = new BailCase();
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(new BailCase());
        when(handler.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
        when(otherHandler.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
        when(handler.canHandle(eq(PreSubmitCallbackStage.ABOUT_TO_SUBMIT), any())).thenReturn(true);
        when(otherHandler.canHandle(eq(PreSubmitCallbackStage.ABOUT_TO_SUBMIT), any())).thenReturn(false);
        when(handler.handle(eq(PreSubmitCallbackStage.ABOUT_TO_SUBMIT), any()))
            .thenReturn(new PreSubmitCallbackResponse<>(updatedCase));

        BailPreSubmitCallbackDispatcher dispatcher =
            new BailPreSubmitCallbackDispatcher(ccdEventAuthorizor, List.of(handler, otherHandler));

        PreSubmitCallbackResponse<BailCase> response =
            dispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(updatedCase, response.getData());
        verify(ccdEventAuthorizor).throwIfNotAuthorized(Event.RECORD_THE_DECISION);
        verify(otherHandler, never()).handle(any(), any());
    }
}
