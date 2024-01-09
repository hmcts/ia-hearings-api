package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CreateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AutoCreateHearingRequestHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HearingService hearingService;
    @Mock
    private CreateHearingPayloadService createHearingPayloadService;
    @Mock
    private CreateHearingRequest createHearingRequest;

    private AutoCreateHearingRequestHandler handler;


    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(1L);
        when(callback.getEvent()).thenReturn(LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);

        handler = new AutoCreateHearingRequestHandler(hearingService, createHearingPayloadService);
    }

    @Test
    void should_handle_successfully() {
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
        when(createHearingPayloadService.buildCreateHearingRequest(caseDetails))
            .thenReturn(createHearingRequest);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, times(1)).createHearing(createHearingRequest);
        verify(asylumCase, times(1)).write(MANUAL_CREATE_HEARINGS_REQUIRED, NO);
    }

    @Test
    void should_throw_exception_if_call_unsuccessful() {
        when(createHearingPayloadService.buildCreateHearingRequest(caseDetails))
            .thenReturn(createHearingRequest);
        when(hearingService.createHearing(createHearingRequest)).thenThrow(new IllegalStateException());

        assertThrows(IllegalStateException.class, () -> handler.handle(ABOUT_TO_SUBMIT, callback));

        verify(asylumCase, never()).write(MANUAL_CREATE_HEARINGS_REQUIRED, NO);
    }

}
