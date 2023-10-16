package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HANDLE_HEARING_EXCEPTION;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.webjars.NotFoundException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HearingExceptionHandlerTest {

    private static final String CASE_REF = "1111";
    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    AsylumCase asylumCase;

    private HearingExceptionHandler hearingExceptionHandler;

    @BeforeEach
    public void setUp() {
        hearingExceptionHandler = new HearingExceptionHandler(coreCaseDataService);
    }

    @Test
    void should_have_early_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, hearingExceptionHandler.getDispatchPriority());
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));
        assertFalse(hearingExceptionHandler.canHandle(serviceData));
    }

    @Test
    void should_handle_if_hmc_status_qualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.EXCEPTION));
        assertTrue(hearingExceptionHandler.canHandle(serviceData));
    }

    @Test
    void should_throw_error_if_cannot_handle() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));

        assertThrows(IllegalStateException.class, () -> hearingExceptionHandler.handle(serviceData));
    }

    @Test
    void should_trigger_handle_hearing_exception_event() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.EXCEPTION));
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class))
            .thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.getCase(CASE_REF)).thenReturn(asylumCase);

        hearingExceptionHandler.handle(serviceData);

        verify(coreCaseDataService).triggerEvent(HANDLE_HEARING_EXCEPTION, CASE_REF, asylumCase);
    }

    @Test
    void should_throw_error_if_case_not_found() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.EXCEPTION));
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class))
            .thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.getCase(CASE_REF)).thenThrow(new NotFoundException("Case not found"));

        hearingExceptionHandler.handle(serviceData);

        verify(coreCaseDataService, never()).triggerEvent(HANDLE_HEARING_EXCEPTION, CASE_REF, asylumCase);
    }
}

