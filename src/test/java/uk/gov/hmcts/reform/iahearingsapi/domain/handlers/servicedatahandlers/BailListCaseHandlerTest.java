package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.DATE_OF_COMPLIANCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.SEND_BAIL_DIRECTION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.COSTS;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BailListCaseHandlerTest {

    private static final String GLASGOW_EPIMMS_ID = "366559";
    private static final String CASE_REF = "1111";
    private static final LocalDateTime NEXT_HEARING_DATE = LocalDateTime.of(2023, 9, 29, 12, 0);
    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    StartEventResponse startEventResponse;
    @Mock
    BailCase bailCase;

    @Captor
    private ArgumentCaptor<BailCaseFieldDefinition> bailExtractorCaptor;

    @Captor
    private ArgumentCaptor<String> bailValueCaptor;

    private BailListCaseHandler bailListCaseHandler;

    @BeforeEach
    public void setUp() {

        bailListCaseHandler =
            new BailListCaseHandler(coreCaseDataService);

        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.LISTED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.FIXED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(BAIL.getKey()));
        when(coreCaseDataService.getCaseState(CASE_REF)).thenReturn(State.LISTING);
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
    }

    @Test
    void should_have_early_dispatch_priority() {
        Assertions.assertEquals(DispatchPriority.EARLY, bailListCaseHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        Assertions.assertTrue(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(COSTS.getKey()));
        Assertions.assertFalse(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));
        Assertions.assertFalse(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_channels_on_papers() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.of(List.of(HearingChannel.ONPPRS)));
        Assertions.assertFalse(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_listing_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.DRAFT));
        Assertions.assertFalse(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_trigger_case_listing() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(SEND_BAIL_DIRECTION, CASE_REF, "Bail")).thenReturn(startEventResponse);
        when(coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse)).thenReturn(bailCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.VID)));
        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(NEXT_HEARING_DATE));

        bailListCaseHandler.handle(serviceData);

        verify(bailCase, times(3)).write(
            bailExtractorCaptor.capture(),
            bailValueCaptor.capture());

        List<BailCaseFieldDefinition> extractors = bailExtractorCaptor.getAllValues();
        List<String> asylumCaseValues = bailValueCaptor.getAllValues();

        assertThat(
            asylumCaseValues.get(extractors.indexOf(SEND_DIRECTION_DESCRIPTION)))
            .containsSequence("You must upload the Bail Summary by the date indicated below.");
        verify(bailCase).write(SEND_DIRECTION_LIST, "Home Office");
        verify(bailCase).write(DATE_OF_COMPLIANCE,
                                 LocalDateTime.of(2023, 9, 28, 9, 45)
                                     .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        verify(coreCaseDataService).triggerBailSubmitEvent(SEND_BAIL_DIRECTION, CASE_REF, startEventResponse, bailCase);
    }
}
