package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_LISTING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMC_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HEARING_COMPLETED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.COSTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

class BailHearingCompletedHandlerTest {
    private static final String CASE_REF = "1111";
    private static final LocalDateTime NEXT_HEARING_DATE = LocalDateTime.of(2023, 9, 29, 12, 0);
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private ServiceData serviceData;
    @Mock
    private StartEventResponse startEventResponse;
    @Mock
    private BailCase bailCase;

    private BailHearingCompletedHandler bailHearingCompletedHandler;

    @BeforeEach
    public void setUp() {
        openMocks(this);
        bailHearingCompletedHandler =
            new BailHearingCompletedHandler(coreCaseDataService);

        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class))
            .thenReturn(Optional.of(CASE_REF));
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.COMPLETED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(BAIL.getKey()));
        when(serviceData.read(HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.FIXED));
    }

    @Test
    void should_have_early_dispatch_priority() {
        Assertions.assertEquals(DispatchPriority.EARLY, bailHearingCompletedHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        Assertions.assertTrue(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(COSTS.getKey()));
        Assertions.assertFalse(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));
        Assertions.assertFalse(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_listing_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.DRAFT));
        Assertions.assertFalse(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_trigger_hearing_completed() {
        when(coreCaseDataService.startCaseEvent(HEARING_COMPLETED, CASE_REF, CASE_TYPE_BAIL))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse)).thenReturn(bailCase);

        bailHearingCompletedHandler.handle(serviceData);

        verify(coreCaseDataService).triggerBailSubmitEvent(HEARING_COMPLETED, CASE_REF,
                                                           startEventResponse, bailCase);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(serviceData.read(HMC_STATUS, HmcStatus.class)).thenReturn(Optional.of(HmcStatus.LISTED));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> bailHearingCompletedHandler.handle(serviceData))
            .hasMessage("Cannot handle service data")
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
