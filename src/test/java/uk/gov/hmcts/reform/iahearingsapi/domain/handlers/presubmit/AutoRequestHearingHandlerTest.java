package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AutoRequestHearingHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HearingService hearingService;

    private AutoRequestHearingHandler handler;


    @BeforeEach
    void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(ON_HEARING_DATE));
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(YES));

        handler = new AutoRequestHearingHandler(hearingService);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "LIST_CASE_WITHOUT_HEARING_REQUIREMENTS",
        "DECISION_AND_REASONS_STARTED",
        "REVIEW_HEARING_REQUIREMENTS",
        "RECORD_ADJOURNMENT_DETAILS",
        "RESTORE_STATE_FROM_ADJOURN"})
    void should_handle_successfully(Event event) {
        when(callback.getEvent()).thenReturn(event);

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
    void should_fail_to_handle_if_record_adjustment_event_and_not_relist_imediately() {
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(NO));

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_fail_to_handle_if_record_adjustment_event_and_adjourned_before_hearing_day() {
        when(callback.getEvent()).thenReturn(RECORD_ADJOURNMENT_DETAILS);
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(BEFORE_HEARING_DATE));

        assertThatThrownBy(() -> handler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "LIST_CASE_WITHOUT_HEARING_REQUIREMENTS",
        "DECISION_AND_REASONS_STARTED",
        "REVIEW_HEARING_REQUIREMENTS",
        "RECORD_ADJOURNMENT_DETAILS",
        "RESTORE_STATE_FROM_ADJOURN"})
    void should_send_hearing_creation_request_to_hmc(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(hearingService.createHearingWithPayload(callback)).thenReturn(asylumCase);

        handler.handle(ABOUT_TO_SUBMIT, callback);

        verify(hearingService, times(1)).createHearingWithPayload(callback);
    }

    @Test
    void should_throw_exception_if_call_unsuccessful() {
        when(callback.getEvent()).thenReturn(LIST_CASE_WITHOUT_HEARING_REQUIREMENTS);
        when(hearingService.createHearingWithPayload(callback)).thenThrow(new IllegalStateException());

        assertThrows(IllegalStateException.class, () -> handler.handle(ABOUT_TO_SUBMIT, callback));

        verify(asylumCase, never()).write(MANUAL_CREATE_HEARINGS_REQUIRED, NO);
    }

}
