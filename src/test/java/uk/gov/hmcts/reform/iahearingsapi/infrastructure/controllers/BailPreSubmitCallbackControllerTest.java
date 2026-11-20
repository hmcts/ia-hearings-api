package uk.gov.hmcts.reform.iahearingsapi.infrastructure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.BailPreSubmitCallbackDispatcher;

@ExtendWith(MockitoExtension.class)
class BailPreSubmitCallbackControllerTest {

    @Mock
    private BailPreSubmitCallbackDispatcher callbackDispatcher;
    @Mock
    private PreSubmitCallbackResponse<BailCase> callbackResponse;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;

    private BailPreSubmitCallbackController controller;

    @BeforeEach
    void setUp() {
        controller = new BailPreSubmitCallbackController(callbackDispatcher);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
    }

    @Test
    void should_dispatch_about_to_start_callback() {
        when(callbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback)).thenReturn(callbackResponse);

        ResponseEntity<PreSubmitCallbackResponse<BailCase>> response = controller.ccdAboutToStart(callback);

        assertEquals(callbackResponse, response.getBody());
    }

    @Test
    void should_dispatch_mid_event_callback_with_the_page_id() {
        when(callbackDispatcher.handle(PreSubmitCallbackStage.MID_EVENT, callback)).thenReturn(callbackResponse);

        ResponseEntity<PreSubmitCallbackResponse<BailCase>> response =
            controller.ccdMidEvent(callback, "selectHearingRecordAttendeesAndDuration");

        assertEquals(callbackResponse, response.getBody());
        verify(callback).setPageId("selectHearingRecordAttendeesAndDuration");
    }

    @Test
    void should_dispatch_about_to_submit_callback() {
        when(callbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)).thenReturn(callbackResponse);

        ResponseEntity<PreSubmitCallbackResponse<BailCase>> response = controller.ccdAboutToSubmit(callback);

        assertEquals(callbackResponse, response.getBody());
    }
}
