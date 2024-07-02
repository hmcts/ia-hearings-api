package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DECISION_WITHOUT_HEARING_LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMC_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.ONPPRS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@ExtendWith(MockitoExtension.class)
public class DecisionWithoutHearingListedTest {

    private static final String CASE_REFERENCE = "1111222233334444";
    private static final String HEARING_REQ_ID = "2000000001";

    @Mock
    private ServiceData serviceData;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private StartEventResponse startEventResponse;
    @Mock
    private HearingService hearingService;
    @Mock
    private PartiesNotifiedResponse partiesNotifiedResponse;

    private DecisionWithoutHearingListed decisionWithoutHearingListed;

    @BeforeEach
    void setup() {
        decisionWithoutHearingListed = new DecisionWithoutHearingListed(coreCaseDataService, hearingService);
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        when(serviceData.read(HEARING_TYPE, String.class)).thenReturn(Optional.of(SUBSTANTIVE.getKey()));
        when(serviceData.read(HMC_STATUS, HmcStatus.class)).thenReturn(Optional.of(LISTED));
        when(serviceData.read(HEARING_CHANNELS, List.class)).thenReturn(Optional.of(List.of(ONPPRS)));
        assertTrue(decisionWithoutHearingListed.canHandle(serviceData));
    }

    @Test
    void should_throw_error_if_cannot_handle() {
        when(serviceData.read(HEARING_TYPE, String.class)).thenReturn(Optional.of(BAIL.getKey()));

        assertThrows(IllegalStateException.class, () -> decisionWithoutHearingListed.handle(serviceData));
    }

    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = { "LISTED", "CANCELLATION_SUBMITTED" })
    void should_trigger_decisionWithoutHearingListed(HmcStatus hmcStatus) {
        when(serviceData.read(HEARING_TYPE, String.class)).thenReturn(Optional.of(SUBSTANTIVE.getKey()));
        when(serviceData.read(HMC_STATUS, HmcStatus.class)).thenReturn(Optional.of(hmcStatus));
        when(serviceData.read(HEARING_CHANNELS, List.class)).thenReturn(Optional.of(List.of(ONPPRS)));
        when(serviceData.read(CASE_REF, String.class)).thenReturn(Optional.of(CASE_REFERENCE));
        when(serviceData.read(HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_REQ_ID));
        when(hearingService.getPartiesNotified(HEARING_REQ_ID)).thenReturn(
            PartiesNotifiedResponses.builder()
                .hearingID(HEARING_REQ_ID)
                .responses(Collections.emptyList())
                .build()
        );
        when(coreCaseDataService.startCaseEvent(
            Event.DECISION_WITHOUT_HEARING_LISTED,
            CASE_REFERENCE,
            CASE_TYPE_ASYLUM)).thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);

        decisionWithoutHearingListed.handle(serviceData);

        verify(coreCaseDataService, times(1)).startCaseEvent(
            Event.DECISION_WITHOUT_HEARING_LISTED,
            CASE_REFERENCE,
            CASE_TYPE_ASYLUM
        );

        verify(asylumCase, times(1))
            .write(DECISION_WITHOUT_HEARING_LISTED, hmcStatus == LISTED ? YES : NO);

        verify(coreCaseDataService, times(1)).triggerSubmitEvent(
            Event.DECISION_WITHOUT_HEARING_LISTED,
            CASE_REFERENCE,
            startEventResponse,
            asylumCase
        );
    }

    @Test
    void should_not_trigger_decisionWithoutHearingListed_if_responses_empty() {
        when(serviceData.read(HEARING_TYPE, String.class)).thenReturn(Optional.of(SUBSTANTIVE.getKey()));
        when(serviceData.read(HMC_STATUS, HmcStatus.class)).thenReturn(Optional.of(LISTED));
        when(serviceData.read(HEARING_CHANNELS, List.class)).thenReturn(Optional.of(List.of(ONPPRS)));
        when(serviceData.read(HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_REQ_ID));
        when(hearingService.getPartiesNotified(HEARING_REQ_ID)).thenReturn(
            PartiesNotifiedResponses.builder()
                .hearingID(HEARING_REQ_ID)
                .responses(List.of(partiesNotifiedResponse))
                .build()
        );

        decisionWithoutHearingListed.handle(serviceData);

        verify(coreCaseDataService, never()).startCaseEvent(any(),any(),any());
        verify(asylumCase, never()).write(eq(DECISION_WITHOUT_HEARING_LISTED), any());
        verify(coreCaseDataService, never()).triggerSubmitEvent(any(),any(),any(),any());
    }
}
