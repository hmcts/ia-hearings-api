package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.HEARING_REQUESTED;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EndAppealRequestSubmitHandlerTest {

    public static final String HEARING_ID = "001";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private HearingService hearingService;
    @Mock
    private AsylumCase asylumCase;
    @Mock private ResponseEntity<HmcHearingResponse> responseEntity;
    EndAppealRequestSubmitHandler endAppealRequestSubmitHandler;

    @BeforeEach
    void setUp() {
        when(asylumCase.read(END_APPEAL_OUTCOME)).thenReturn(Optional.of("Withdrawn"));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(END_APPEAL);

        endAppealRequestSubmitHandler = new EndAppealRequestSubmitHandler(hearingService);
    }

    @Test
    void should_handle_successfully() {
        assertTrue(endAppealRequestSubmitHandler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_fail_to_handle_if_invalid_event() {
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        assertThatThrownBy(() -> endAppealRequestSubmitHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = {"HEARING_REQUESTED", "AWAITING_LISTING", "UPDATE_REQUESTED",
        "UPDATE_SUBMITTED", "LISTED"})
    void should_delete_hearings_with_certain_statuses(HmcStatus status) {
        HearingsGetResponse hearingsResponse = getHearingsGetResponse(status);

        when(hearingService.getHearings(any())).thenReturn(hearingsResponse);
        when(hearingService.deleteHearing(any(), any())).thenReturn(responseEntity);

        endAppealRequestSubmitHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, times(2)).deleteHearing(eq(Long.valueOf(HEARING_ID)), eq("withdraw"));
    }

    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = {"CLOSED", "CANCELLED", "CANCELLATION_REQUESTED",
        "CANCELLATION_SUBMITTED", "EXCEPTION"})
    void should_not_delete_hearings_with_certain_statuses(HmcStatus status) {
        HearingsGetResponse hearingsResponse = getHearingsGetResponse(status);

        when(hearingService.getHearings(any())).thenReturn(hearingsResponse);
        when(hearingService.deleteHearing(any(), any())).thenReturn(responseEntity);

        endAppealRequestSubmitHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, never()).deleteHearing(any(), any());
    }

    @Test
    void should_require_manual_cancel_hearing_if_delete_hearing_request_failed() {
        HearingsGetResponse hearingsResponse = getHearingsGetResponse(HEARING_REQUESTED);

        when(hearingService.getHearings(any())).thenReturn(hearingsResponse);
        when(hearingService.deleteHearing(any(), any()))
            .thenThrow(new HmcException(new Throwable()));

        endAppealRequestSubmitHandler.handle(
            ABOUT_TO_SUBMIT,
            callback
        );

        verify(asylumCase).write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);
    }

    private static HearingsGetResponse getHearingsGetResponse(HmcStatus status) {
        CaseHearing hearing1 = CaseHearing.builder()
            .hearingRequestId(HEARING_ID)
            .hmcStatus(status)
            .build();

        CaseHearing hearing2 = CaseHearing.builder()
            .hearingRequestId(HEARING_ID)
            .hmcStatus(status)
            .build();

        HearingsGetResponse hearingsResponse = HearingsGetResponse.builder()
            .caseRef("101")
            .caseHearings(List.of(hearing1, hearing2))
            .hmctsServiceCode("code")
            .build();
        return hearingsResponse;
    }

}