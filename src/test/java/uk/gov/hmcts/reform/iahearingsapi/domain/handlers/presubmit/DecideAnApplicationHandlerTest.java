package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DECISION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.DECIDE_AN_APPLICATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.COMPLETED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HmcHearingResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class DecideAnApplicationHandlerTest {

    private static final long CASE_ID = 4444111122223333L;
    private static final String HEARING_ID_1 = "1";
    private static final String HEARING_ID_2 = "2";
    private static final String HEARING_ID_3 = "3";
    private static final String HEARING_ID_4 = "4";
    private static final String DECISION_REASON = "Some reason";

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
    private CaseHearing substantiveListed;
    @Mock
    private CaseHearing substantiveAwaitingListing;
    @Mock
    private CaseHearing substantiveCompleted;
    @Mock
    private CaseHearing costsListed;
    @Mock
    private ResponseEntity<HmcHearingResponse> responseEntity;
    @Mock
    private HmcException hmcException;

    private DecideAnApplicationHandler decideAnApplicationHandler;

    @BeforeEach
    void setup() {
        decideAnApplicationHandler = new DecideAnApplicationHandler(hearingService);
    }

    @Test
    void should_cancel_all_substantive_hearing_requests() {
        when(hearingService.getHearings(CASE_ID)).thenReturn(hearingsGetResponse);
        when(hearingsGetResponse.getCaseHearings()).thenReturn(List.of(substantiveListed,
                                                                       substantiveAwaitingListing,
                                                                       substantiveCompleted,
                                                                       costsListed
        ));
        when(substantiveListed.getHearingRequestId()).thenReturn(HEARING_ID_1);
        when(substantiveListed.getHearingType()).thenReturn(HearingType.SUBSTANTIVE.getKey());
        when(substantiveListed.getHmcStatus()).thenReturn(LISTED);

        when(substantiveAwaitingListing.getHearingRequestId()).thenReturn(HEARING_ID_2);
        when(substantiveAwaitingListing.getHearingType()).thenReturn(HearingType.SUBSTANTIVE.getKey());
        when(substantiveAwaitingListing.getHmcStatus()).thenReturn(AWAITING_LISTING);

        when(substantiveCompleted.getHearingRequestId()).thenReturn(HEARING_ID_3);
        when(substantiveCompleted.getHearingType()).thenReturn(HearingType.SUBSTANTIVE.getKey());
        when(substantiveCompleted.getHmcStatus()).thenReturn(COMPLETED);

        when(costsListed.getHearingRequestId()).thenReturn(HEARING_ID_4);
        when(costsListed.getHearingType()).thenReturn(HearingType.COSTS.getKey());
        when(costsListed.getHmcStatus()).thenReturn(LISTED);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION_REASON, String.class))
            .thenReturn(Optional.of(DECISION_REASON));

        when(callback.getEvent()).thenReturn(DECIDE_AN_APPLICATION);

        when(hearingService.deleteHearing(anyLong(), anyString())).thenReturn(responseEntity);
        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        decideAnApplicationHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, times(1)).deleteHearing(1L, DECISION_REASON);
        verify(hearingService, times(1)).deleteHearing(2L, DECISION_REASON);
        verify(hearingService, never()).deleteHearing(eq(3L), anyString());
        verify(hearingService, never()).deleteHearing(eq(4L), anyString());
        verify(asylumCase, times(1)).write(MANUAL_CANCEL_HEARINGS_REQUIRED, NO);
    }

    @Test
    void should_write_when_cancellation_unsuccessful() {
        when(hearingService.getHearings(CASE_ID)).thenReturn(hearingsGetResponse);
        when(hearingsGetResponse.getCaseHearings()).thenReturn(List.of(substantiveListed,
                                                                       substantiveAwaitingListing,
                                                                       substantiveCompleted,
                                                                       costsListed
        ));
        when(substantiveListed.getHearingRequestId()).thenReturn(HEARING_ID_1);
        when(substantiveListed.getHearingType()).thenReturn(HearingType.SUBSTANTIVE.getKey());
        when(substantiveListed.getHmcStatus()).thenReturn(LISTED);

        when(substantiveAwaitingListing.getHearingRequestId()).thenReturn(HEARING_ID_2);
        when(substantiveAwaitingListing.getHearingType()).thenReturn(HearingType.SUBSTANTIVE.getKey());
        when(substantiveAwaitingListing.getHmcStatus()).thenReturn(AWAITING_LISTING);

        when(substantiveCompleted.getHearingRequestId()).thenReturn(HEARING_ID_3);
        when(substantiveCompleted.getHearingType()).thenReturn(HearingType.SUBSTANTIVE.getKey());
        when(substantiveCompleted.getHmcStatus()).thenReturn(COMPLETED);

        when(costsListed.getHearingRequestId()).thenReturn(HEARING_ID_4);
        when(costsListed.getHearingType()).thenReturn(HearingType.COSTS.getKey());
        when(costsListed.getHmcStatus()).thenReturn(LISTED);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(asylumCase.read(MAKE_AN_APPLICATION_DECISION_REASON, String.class))
            .thenReturn(Optional.of(DECISION_REASON));

        when(callback.getEvent()).thenReturn(DECIDE_AN_APPLICATION);

        when(hearingService.deleteHearing(anyLong(), anyString())).thenThrow(hmcException);
        decideAnApplicationHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, times(1)).deleteHearing(1L, DECISION_REASON);
        verify(hearingService, never()).deleteHearing(eq(3L), anyString());
        verify(hearingService, never()).deleteHearing(eq(4L), anyString());
        verify(asylumCase, times(1)).write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> decideAnApplicationHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideAnApplicationHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = decideAnApplicationHandler.canHandle(callbackStage, callback);

                if ((event == Event.DECIDE_AN_APPLICATION)
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}
