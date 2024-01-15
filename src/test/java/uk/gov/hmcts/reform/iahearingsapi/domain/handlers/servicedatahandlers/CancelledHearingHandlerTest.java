package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.CASE_MANAGEMENT_REVIEW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CancelledHearingHandlerTest {
    private static final String CASE_REF = "1234";

    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    StartEventResponse startEventResponse;
    @Mock
    AsylumCase asylumCase;
    private CancelledHearingHandler cancelledHearingHandler;

    @BeforeEach
    public void setUp() {

        cancelledHearingHandler =
            new CancelledHearingHandler(coreCaseDataService);

        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CANCELLED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(SUBSTANTIVE.getKey()));
    }

    @Test
    void should_have_early_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, cancelledHearingHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        assertTrue(cancelledHearingHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(CASE_MANAGEMENT_REVIEW.getKey()));
        assertFalse(cancelledHearingHandler.canHandle(serviceData));
    }

    @Test
    void should_throw_message_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(CASE_MANAGEMENT_REVIEW.getKey()));

        assertThatThrownBy(() -> cancelledHearingHandler.handle(serviceData))
            .hasMessage("Cannot handle service data")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_trigger_review_interpreter_booking_task() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));

        cancelledHearingHandler.handle(serviceData);

        verify(coreCaseDataService).triggerReviewInterpreterBookingTask(CASE_REF);
    }

}