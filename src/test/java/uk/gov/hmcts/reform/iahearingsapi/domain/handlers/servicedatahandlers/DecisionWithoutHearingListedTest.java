package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DECISION_WITHOUT_HEARING_LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMC_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.ONPPRS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@ExtendWith(MockitoExtension.class)
public class DecisionWithoutHearingListedTest {

    private static final String CASE_REFERNECE = "1111222233334444";

    @Mock
    private ServiceData serviceData;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private StartEventResponse startEventResponse;

    private DecisionWithoutHearingListed decisionWithoutHearingListed;

    @BeforeEach
    void setup() {
        decisionWithoutHearingListed = new DecisionWithoutHearingListed(coreCaseDataService);
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        when(serviceData.read(HMC_STATUS, HmcStatus.class)).thenReturn(Optional.of(LISTED));
        when(serviceData.read(HEARING_CHANNELS, List.class)).thenReturn(Optional.of(List.of(ONPPRS)));
        assertTrue(decisionWithoutHearingListed.canHandle(serviceData));
    }

    @Test
    void should_throw_error_if_cannot_handle() {
        when(serviceData.read(HMC_STATUS, HmcStatus.class)).thenReturn(Optional.of(HEARING_REQUESTED));
        when(serviceData.read(HEARING_CHANNELS, List.class)).thenReturn(Optional.of(List.of(ONPPRS)));

        assertThrows(IllegalStateException.class, () -> decisionWithoutHearingListed.handle(serviceData));
    }

    @Test
    void should_trigger_decisionWithoutHearingListed() {
        when(serviceData.read(HMC_STATUS, HmcStatus.class)).thenReturn(Optional.of(LISTED));
        when(serviceData.read(HEARING_CHANNELS, List.class)).thenReturn(Optional.of(List.of(ONPPRS)));
        when(serviceData.read(CASE_REF, String.class)).thenReturn(Optional.of(CASE_REFERNECE));
        when(coreCaseDataService.startCaseEvent(
            Event.DECISION_WITHOUT_HEARING_LISTED,
            CASE_REFERNECE,
            CASE_TYPE_ASYLUM)).thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);
        decisionWithoutHearingListed.handle(serviceData);

        verify(coreCaseDataService, times(1)).startCaseEvent(
            Event.DECISION_WITHOUT_HEARING_LISTED,
            CASE_REFERNECE,
            CASE_TYPE_ASYLUM
        );

        verify(asylumCase, times(1)).write(DECISION_WITHOUT_HEARING_LISTED, YES);

        verify(coreCaseDataService, times(1)).triggerSubmitEvent(
            Event.DECISION_WITHOUT_HEARING_LISTED,
            CASE_REFERNECE,
            startEventResponse,
            asylumCase
        );

    }
}
