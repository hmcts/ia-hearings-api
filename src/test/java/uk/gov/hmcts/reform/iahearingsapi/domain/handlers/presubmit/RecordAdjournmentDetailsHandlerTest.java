package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HMC_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordAdjournmentDetailsHandlerTest {

    public static final String CANCELLATION_REASON = "withdraw";
    public static final String HEARING_ID = "123";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private HearingService hearingService;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private ResponseEntity<HmcHearingResponse> responseEntity;
    private RecordAdjournmentDetailsHandler recordAdjournmentDetailsHandler;

    @BeforeEach
    void setUp() {
        when(asylumCase.read(HEARING_CANCELLATION_REASON, String.class))
            .thenReturn(Optional.of(CANCELLATION_REASON));

        when(asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList(HEARING_ID)));

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);

        recordAdjournmentDetailsHandler = new RecordAdjournmentDetailsHandler(hearingService);
    }

    @Test
    void should_handle_successfully() {
        assertTrue(recordAdjournmentDetailsHandler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_fail_to_handle_if_invalid_event() {
        when(callback.getEvent()).thenReturn(UPDATE_HMC_RESPONSE);
        assertThatThrownBy(() -> recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_delete_hearing() {
        when(hearingService.deleteHearing(any(), any())).thenReturn(responseEntity);

        recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, times(1))
            .deleteHearing(ArgumentMatchers.eq(Long.valueOf(HEARING_ID)), eq(CANCELLATION_REASON));
    }

    @Test
    void should_require_manual_cancel_hearing_if_delete_hearing_request_failed() {
        when(hearingService.deleteHearing(any(), any()))
            .thenThrow(new HmcException(new Throwable()));

        recordAdjournmentDetailsHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);
    }
}