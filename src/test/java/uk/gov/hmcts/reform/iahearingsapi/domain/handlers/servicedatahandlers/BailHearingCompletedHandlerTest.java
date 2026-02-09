package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_LISTING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMC_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.COSTS;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

class BailHearingCompletedHandlerTest {
    private static final String CASE_REF = "1111";
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private ServiceData serviceData;

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
        assertEquals(DispatchPriority.EARLY, bailHearingCompletedHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        assertTrue(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(COSTS.getKey()));
        assertFalse(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));
        assertFalse(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_trigger_hearing_completedOrCancelled() {
        bailHearingCompletedHandler.handle(serviceData);

        verify(coreCaseDataService).hearingCompletedOrCancelledTask(CASE_REF);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(serviceData.read(HMC_STATUS, HmcStatus.class)).thenReturn(Optional.of(HmcStatus.LISTED));
        IllegalStateException exception =
            assertThrows(IllegalStateException.class, () -> bailHearingCompletedHandler.handle(serviceData));
        assertEquals("Cannot handle service data", exception.getMessage());
    }
}
