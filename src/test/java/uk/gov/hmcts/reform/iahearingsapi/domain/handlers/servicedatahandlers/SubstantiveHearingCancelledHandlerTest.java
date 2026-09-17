package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_INTERPRETER_SERVICES_NEEDED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HEARING_CANCELLED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.CASE_MANAGEMENT_REVIEW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.NextHearingDateService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class SubstantiveHearingCancelledHandlerTest {
    private static final String CASE_ID = "1234";

    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    StartEventResponse startEventResponse;
    @Mock
    ServiceData serviceData;
    @Mock
    AsylumCase asylumCase;
    @Mock
    NextHearingDateService nextHearingDateService;

    private SubstantiveHearingCancelledHandler substantiveHearingCancelledHandler;

    @BeforeEach
    public void setUp() {

        substantiveHearingCancelledHandler =
            new SubstantiveHearingCancelledHandler(coreCaseDataService, nextHearingDateService);

        when(serviceData.read(CASE_REF, String.class)).thenReturn(Optional.of(CASE_ID));
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CANCELLED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(SUBSTANTIVE.getKey()));
        when(coreCaseDataService.startCaseEvent(HEARING_CANCELLED, CASE_ID, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse))
            .thenReturn(asylumCase);
    }

    @Test
    void should_have_early_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, substantiveHearingCancelledHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        assertTrue(substantiveHearingCancelledHandler.canHandle(serviceData));
    }

    @Test
    void isSubstantiveCancelledHearing() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER, TEL, VID, HearingChannel.NA)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE))
            .thenReturn(Optional.of(SUBSTANTIVE.getKey()));
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS))
            .thenReturn(Optional.of(HmcStatus.CANCELLED));

        assertTrue(substantiveHearingCancelledHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(CASE_MANAGEMENT_REVIEW.getKey()));
        assertFalse(substantiveHearingCancelledHandler.canHandle(serviceData));
    }

    @Test
    void should_throw_message_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(CASE_MANAGEMENT_REVIEW.getKey()));

        assertThatThrownBy(() -> substantiveHearingCancelledHandler.handle(serviceData))
            .hasMessage("Cannot handle service data")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_trigger_submit_event_when_next_hearing_date_enabled() {
        when(nextHearingDateService.enabled()).thenReturn(true);

        substantiveHearingCancelledHandler.handle(serviceData);

        verify(coreCaseDataService).triggerSubmitEvent(
            HEARING_CANCELLED, CASE_ID, startEventResponse, asylumCase);
    }

    @Test
    void should_not_trigger_submit_event_when_next_hearing_date_not_enabled() {
        when(nextHearingDateService.enabled()).thenReturn(false);

        substantiveHearingCancelledHandler.handle(serviceData);

        verify(coreCaseDataService, never()).triggerSubmitEvent(
            HEARING_CANCELLED, CASE_ID, startEventResponse, asylumCase);
    }

    @Test
    void should_trigger_review_interpreter_task_when_interpreter_needed() {
        when(nextHearingDateService.enabled()).thenReturn(true);
        initializeInterpreterData();

        substantiveHearingCancelledHandler.handle(serviceData);

        verify(asylumCase).write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
    }

    @Test
    void should_clear_review_interpreter_task_when_interpreter_not_needed() {
        when(nextHearingDateService.enabled()).thenReturn(true);
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class))
            .thenReturn(Optional.of(NO));

        substantiveHearingCancelledHandler.handle(serviceData);

        verify(asylumCase, never()).write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
        verify(asylumCase).clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
    }

    @Test
    void should_not_interact_with_asylum_case_when_next_hearing_date_not_enabled() {
        when(nextHearingDateService.enabled()).thenReturn(false);

        substantiveHearingCancelledHandler.handle(serviceData);

        verify(coreCaseDataService, never()).startCaseEvent(HEARING_CANCELLED, CASE_ID, CASE_TYPE_ASYLUM);
        verify(asylumCase, never()).write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
        verify(asylumCase, never()).clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
    }

    @Test
    void should_trigger_review_interpreter_task_when_sign_language_only() {
        when(nextHearingDateService.enabled()).thenReturn(true);
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class))
            .thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of("signLanguageInterpreter")));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE))
            .thenReturn(Optional.empty());
        InterpreterLanguageRefData signLanguage = new InterpreterLanguageRefData();
        signLanguage.setLanguageRefData(new DynamicList(new Value("bsl", "British Sign Language"), List.of()));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn(Optional.of(signLanguage));

        substantiveHearingCancelledHandler.handle(serviceData);

        verify(asylumCase).write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
    }

    @Test
    void should_clear_review_interpreter_task_when_no_language_category() {
        when(nextHearingDateService.enabled()).thenReturn(true);
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class))
            .thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.empty());

        substantiveHearingCancelledHandler.handle(serviceData);

        verify(asylumCase, never()).write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
        verify(asylumCase).clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
    }

    @Test
    void should_clear_review_interpreter_task_when_language_ref_data_is_null() {
        when(nextHearingDateService.enabled()).thenReturn(true);
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class))
            .thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of("spokenLanguageInterpreter")));
        InterpreterLanguageRefData spokenLanguage = new InterpreterLanguageRefData();
        spokenLanguage.setLanguageRefData(null);
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE))
            .thenReturn(Optional.of(spokenLanguage));
        when(asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE))
            .thenReturn(Optional.empty());

        substantiveHearingCancelledHandler.handle(serviceData);

        verify(asylumCase, never()).write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
        verify(asylumCase).clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
    }

    private void initializeInterpreterData() {
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class))
            .thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY))
            .thenReturn(Optional.of(List.of("spokenLanguageInterpreter")));
        InterpreterLanguageRefData spokenLanguage = new InterpreterLanguageRefData();
        spokenLanguage.setLanguageRefData(new DynamicList(new Value("eng", "English"), List.of()));
        when(asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE))
            .thenReturn(Optional.of(spokenLanguage));
    }
}
