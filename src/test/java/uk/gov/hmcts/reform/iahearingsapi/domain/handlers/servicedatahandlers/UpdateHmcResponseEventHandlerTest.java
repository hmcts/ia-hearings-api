package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_HMC_RESPONSE;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class UpdateHmcResponseEventHandlerTest {

    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    AsylumCase asylumCase;
    private static final String CASE_REF = "1111";
    private UpdateHmcResponseEventHandler handler;

    @BeforeEach
    public void setUp() {
        handler =
            new UpdateHmcResponseEventHandler(coreCaseDataService);

        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.LISTED));
    }

    @Test
    void should_have_late_dispatch_priority() {
        assertEquals(DispatchPriority.LATE, handler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        assertTrue(handler.canHandle(serviceData));
    }

    @Test
    void should_throw_error_if_case_ref_missing() {
        assertThrows(IllegalStateException.class, () -> handler.handle(serviceData));
    }

    @Test
    void should_trigger_update_hmc_response_event() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.getCase(CASE_REF)).thenReturn(asylumCase);

        handler.handle(serviceData);

        verify(coreCaseDataService).triggerEvent(UPDATE_HMC_RESPONSE, CASE_REF, asylumCase);
    }

}
