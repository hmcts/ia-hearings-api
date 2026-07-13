package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RE_LIST_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.CASE_MANAGEMENT_REVIEW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RelistHearingHandlerTest {

    private static final String CASE_REF = "1234";
    private static final String HEARING_ID = "2000000001";

    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    StartEventResponse startEventResponse;
    @Mock
    AsylumCase asylumCase;

    private RelistHearingHandler relistHearingHandler;

    @BeforeEach
    public void setUp() {

        relistHearingHandler = new RelistHearingHandler(coreCaseDataService);

        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.AWAITING_LISTING));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(CASE_MANAGEMENT_REVIEW.getKey()));
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));
        when(coreCaseDataService.getCase(CASE_REF)).thenReturn(asylumCase);
        when(asylumCase.read(CURRENT_HEARING_ID, String.class)).thenReturn(Optional.of(HEARING_ID));
    }

    @Test
    void should_have_early_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, relistHearingHandler.getDispatchPriority());
    }

    @ParameterizedTest
    @EnumSource(value = HearingType.class, names = { "CASE_MANAGEMENT_REVIEW", "SUBSTANTIVE" })
    void should_handle_if_awaiting_listing_for_already_listed_hearing(HearingType hearingType) {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(hearingType.getKey()));

        assertTrue(relistHearingHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.LISTED));

        assertFalse(relistHearingHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_channels_on_papers() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.of(List.of(HearingChannel.ONPPRS)));

        assertFalse(relistHearingHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(BAIL.getKey()));

        assertFalse(relistHearingHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_initial_listing_when_no_current_hearing_id() {
        when(asylumCase.read(CURRENT_HEARING_ID, String.class)).thenReturn(Optional.empty());

        assertFalse(relistHearingHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_initial_listing_when_current_hearing_id_is_different() {
        when(asylumCase.read(CURRENT_HEARING_ID, String.class)).thenReturn(Optional.of("2000000002"));

        assertFalse(relistHearingHandler.canHandle(serviceData));
    }

    @ParameterizedTest
    @EnumSource(value = HearingType.class, names = { "CASE_MANAGEMENT_REVIEW", "SUBSTANTIVE" })
    void should_trigger_re_list_hearing_event(HearingType hearingType) {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(hearingType.getKey()));
        when(coreCaseDataService.startCaseEvent(RE_LIST_HEARING, CASE_REF, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);

        relistHearingHandler.handle(serviceData);

        verify(coreCaseDataService).triggerSubmitEvent(
            RE_LIST_HEARING, CASE_REF, startEventResponse, asylumCase);
    }

    @Test
    void should_throw_if_cannot_handle() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.LISTED));

        assertThrows(IllegalStateException.class, () -> relistHearingHandler.handle(serviceData));
    }
}
