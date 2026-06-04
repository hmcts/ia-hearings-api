package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMC_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.COSTS;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@ExtendWith(MockitoExtension.class)
class BailHearingCompletedHandlerTest {
    private static final String CASE_REF = "1111";
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private ServiceData serviceData;

    private BailHearingCompletedHandler bailHearingCompletedHandler;

    @BeforeEach
    public void setUp() {
        bailHearingCompletedHandler =
            new BailHearingCompletedHandler(coreCaseDataService);
    }

    @Test
    void should_have_early_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, bailHearingCompletedHandler.getDispatchPriority());
    }

    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = {"COMPLETED", "CANCELLED"})
    void should_handle_if_service_data_qualifies_status(HmcStatus hmcStatus) {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(hmcStatus));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(BAIL.getKey()));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.empty());

        assertTrue(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_handle_if_service_data_qualifies_channels_empty() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.COMPLETED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(BAIL.getKey()));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.empty());

        assertTrue(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @ParameterizedTest
    @EnumSource(value = HearingChannel.class, names = {"ONPPRS"}, mode = EnumSource.Mode.EXCLUDE)
    void should_handle_if_service_data_qualifies_channels_not_contain_onpprs(HearingChannel hearingChannel) {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.COMPLETED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(BAIL.getKey()));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.of(List.of(hearingChannel)));

        assertTrue(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @ParameterizedTest
    @EnumSource(value = HmcStatus.class, names = {"COMPLETED", "CANCELLED"}, mode = EnumSource.Mode.EXCLUDE)
    void should_not_handle_if_hmc_status_unqualified(HmcStatus hmcStatus) {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(hmcStatus));

        assertFalse(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.COMPLETED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.empty());
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(COSTS.getKey()));
        assertFalse(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_channels_contains_onpprs() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.COMPLETED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.of(List.of(HearingChannel.ONPPRS)));

        assertFalse(bailHearingCompletedHandler.canHandle(serviceData));
    }

    @Test
    void should_trigger_hearing_completedOrCancelled() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class))
            .thenReturn(Optional.of(CASE_REF));
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.COMPLETED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(BAIL.getKey()));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.empty());
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
