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
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUIRE_MANUAL_HEARINGS_CANCELLATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HMC_RESPONSE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;

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
        when(callback.getEvent()).thenReturn(UPDATE_HMC_RESPONSE);
        assertThatThrownBy(() -> endAppealRequestSubmitHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = {"HEARING_REQUESTED", "AWAITING_LISTING", "UPDATE_REQUESTED",
        "UPDATE_SUBMITTED", "LISTED"})
    void should_delete_hearings_with_certain_statuses(HmcStatus status) {
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

        when(hearingService.getHearings(any())).thenReturn(hearingsResponse);
        when(hearingService.deleteHearing(any(), any())).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        endAppealRequestSubmitHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, times(2)).deleteHearing(eq(Long.valueOf(HEARING_ID)), eq("withdraw"));
    }

    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = {"CLOSED", "CANCELLED", "CANCELLATION_REQUESTED",
        "CANCELLATION_SUBMITTED", "EXCEPTION"})
    void should_not_delete_hearings_with_certain_statuses(HmcStatus status) {
        CaseHearing hearing1 = CaseHearing.builder()
            .hearingRequestId(HEARING_ID)
            .hmcStatus(status)
            .build();

        HearingsGetResponse hearingsResponse = HearingsGetResponse.builder()
            .caseRef("101")
            .caseHearings(List.of(hearing1))
            .hmctsServiceCode("code")
            .build();

        when(hearingService.getHearings(any())).thenReturn(hearingsResponse);
        when(hearingService.deleteHearing(any(), any())).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

        endAppealRequestSubmitHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, never()).deleteHearing(any(), any());
    }

    @Test
    void should_require_manual_hearing_cancellation_when_delete_request_unsuccessful() {
        CaseHearing hearing1 = CaseHearing.builder()
            .hearingRequestId(HEARING_ID)
            .hmcStatus(HmcStatus.HEARING_REQUESTED)
            .build();

        HearingsGetResponse hearingsResponse = HearingsGetResponse.builder()
            .caseRef("101")
            .caseHearings(List.of(hearing1))
            .hmctsServiceCode("code")
            .build();

        when(hearingService.getHearings(any())).thenReturn(hearingsResponse);
        when(hearingService.deleteHearing(any(), any())).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        endAppealRequestSubmitHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(REQUIRE_MANUAL_HEARINGS_CANCELLATION, YES);
    }

}