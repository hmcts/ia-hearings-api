package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_REQUEST_VERSION_NUMBER;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@ExtendWith(MockitoExtension.class)
public class SendServiceDataToHmcHandlerTest {

    private static final long VERSION_NUMBER = 1L;
    private static final String HEARING_ID = "2000000050";
    private static final LocalDateTime HEARING_RESPONSE_RECEIVED_DATE_TIME = LocalDateTime.of(2023, 9, 29, 11, 0);
    @Mock
    HearingService hearingService;
    @Mock
    ServiceData serviceData;

    private SendServiceDataToHmcHandler sendServiceDataToHmcHandler;

    @BeforeEach
    public void setUp() {
        sendServiceDataToHmcHandler = new SendServiceDataToHmcHandler(hearingService);
    }

    @Test
    void should_have_latest_dispatch_priority() {
        assertEquals(DispatchPriority.LATEST, sendServiceDataToHmcHandler.getDispatchPriority());
    }

    @Test
    void should_always_run() {
        assertTrue(sendServiceDataToHmcHandler.canHandle(serviceData));
    }

    @Test
    void should_trigger_case_listing() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_ID));
        when(serviceData.read(HEARING_REQUEST_VERSION_NUMBER, Long.class)).thenReturn(Optional.of(VERSION_NUMBER));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_RESPONSE_RECEIVED_DATE_TIME, LocalDateTime.class))
            .thenReturn(Optional.of(HEARING_RESPONSE_RECEIVED_DATE_TIME));

        PartiesNotified partiesNotified = PartiesNotified.builder()
            .serviceData(serviceData)
            .build();

        sendServiceDataToHmcHandler.handle(serviceData);

        verify(hearingService).updatePartiesNotified(eq(HEARING_ID),
                                                     eq(VERSION_NUMBER),
                                                     eq(HEARING_RESPONSE_RECEIVED_DATE_TIME),
                                                     eq(partiesNotified));
    }
}