package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.TRIGGER_CMR_LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.TRIGGER_CMR_UPDATED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.CASE_MANAGEMENT_REVIEW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.NextHearingDateService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ListCmrHandlerTest {

    private static final String CASE_REF = "1234";
    public static final String HEARING_ID = "1";

    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    HearingService hearingService;
    @Mock
    ServiceData serviceData;
    @Mock
    StartEventResponse startEventResponse;
    @Mock
    AsylumCase asylumCase;
    @Mock
    NextHearingDateService nextHearingDateService;
    @Captor
    private ArgumentCaptor<NextHearingDetails> nextHearingDetailsArgumentCaptor;

    private ListCmrHandler listCmrHandler;

    private final NextHearingDetails nextHearingDetailsFromHearings = NextHearingDetails
        .builder()
        .hearingId("hearingId")
        .hearingDateTime(LocalDateTime.now().toString())
        .build();
    private final String listCaseHearingDate = LocalDateTime.now().plusDays(1).toString();
    private final NextHearingDetails nextHearingDetailsFromCaseData = NextHearingDetails
        .builder()
        .hearingId("999")
        .hearingDateTime(listCaseHearingDate)
        .build();

    @BeforeEach
    public void setUp() {

        listCmrHandler =
            new ListCmrHandler(coreCaseDataService, hearingService, nextHearingDateService);

        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.LISTED));
        when(serviceData.read(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, ListAssistCaseStatus.class))
            .thenReturn(Optional.of(ListAssistCaseStatus.LISTED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(CASE_MANAGEMENT_REVIEW.getKey()));
        when(coreCaseDataService.getCaseState(CASE_REF)).thenReturn(State.LISTING);
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
    }

    @Test
    void should_have_early_dispatch_priority() {
        assertEquals(DispatchPriority.EARLY, listCmrHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        assertTrue(listCmrHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(SUBSTANTIVE.getKey()));
        assertFalse(listCmrHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));
        assertFalse(listCmrHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_channels_on_papers() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.of(List.of(HearingChannel.ONPPRS)));
        assertFalse(listCmrHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_list_assist_case_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, ListAssistCaseStatus.class))
            .thenReturn(Optional.of(ListAssistCaseStatus.CASE_CLOSED));
        assertFalse(listCmrHandler.canHandle(serviceData));
    }

    @Test
    void should_trigger_cmr_listed_notification() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(TRIGGER_CMR_LISTED, CASE_REF, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));

        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));

        PartiesNotifiedResponses partiesNotifiedResponses =
            PartiesNotifiedResponses.builder()
                .responses(Collections.emptyList())
                .hearingID(HEARING_ID).build();
        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);

        listCmrHandler.handle(serviceData);

        verify(coreCaseDataService).triggerSubmitEvent(
            TRIGGER_CMR_LISTED, CASE_REF, startEventResponse, asylumCase);
    }

    @Test
    void should_trigger_cmr_updated_notification() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(TRIGGER_CMR_UPDATED, CASE_REF, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));

        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));

        PartiesNotifiedResponse response =
            PartiesNotifiedResponse.builder()
                .responseReceivedDateTime(LocalDateTime.parse("2024-09-20T10:09:19"))
                .partiesNotified(LocalDateTime.parse("2024-09-20T10:09:19"))
                .requestVersion(1)
                .build();
        PartiesNotifiedResponses partiesNotifiedResponses =
            PartiesNotifiedResponses.builder()
                .responses(List.of(response))
                .hearingID(HEARING_ID).build();
        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);

        listCmrHandler.handle(serviceData);

        verify(coreCaseDataService).triggerSubmitEvent(
            TRIGGER_CMR_UPDATED, CASE_REF, startEventResponse, asylumCase);
    }

    @Test
    void should_not_trigger_cmr_updated_notification() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(TRIGGER_CMR_UPDATED, CASE_REF, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));

        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));

        ServiceData previousServiceData = new ServiceData();
        previousServiceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS, List.of(HearingChannel.INTER));
        PartiesNotifiedResponse response =
            PartiesNotifiedResponse.builder()
                .responseReceivedDateTime(LocalDateTime.parse("2024-09-20T10:09:19"))
                .partiesNotified(LocalDateTime.parse("2024-09-20T10:09:19"))
                .serviceData(previousServiceData)
                .requestVersion(1)
                .build();

        PartiesNotifiedResponses partiesNotifiedResponses =
            PartiesNotifiedResponses.builder()
                .responses(List.of(response))
                .hearingID(HEARING_ID).build();
        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);

        listCmrHandler.handle(serviceData);

        verify(coreCaseDataService, never()).triggerSubmitEvent(
            TRIGGER_CMR_UPDATED, CASE_REF, startEventResponse, asylumCase);
    }

    @Test
    public void should_not_set_next_hearing_date_if_feature_not_enabled() {
        when(nextHearingDateService.enabled()).thenReturn(false);
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(TRIGGER_CMR_LISTED, CASE_REF, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));

        PartiesNotifiedResponses partiesNotifiedResponses =
            PartiesNotifiedResponses.builder()
                .responses(Collections.emptyList())
                .hearingID(HEARING_ID).build();
        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);

        listCmrHandler.handle(serviceData);

        verify(nextHearingDateService, never()).getNextHearingDetails(anyLong());
        verify(asylumCase, never()).write(eq(NEXT_HEARING_DETAILS), any());
    }

    @Test
    public void should_set_next_hearing_date_from_hearings_on_cmr_listed() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(TRIGGER_CMR_LISTED, CASE_REF, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));

        PartiesNotifiedResponses partiesNotifiedResponses =
            PartiesNotifiedResponses.builder()
                .responses(Collections.emptyList())
                .hearingID(HEARING_ID).build();
        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);

        when(nextHearingDateService.enabled()).thenReturn(true);
        when(nextHearingDateService.getNextHearingDetails(Long.parseLong(CASE_REF)))
            .thenReturn(nextHearingDetailsFromHearings);

        listCmrHandler.handle(serviceData);

        verify(nextHearingDateService, times(1)).getNextHearingDetails(anyLong());
        verify(asylumCase).write(eq(NEXT_HEARING_DETAILS), nextHearingDetailsArgumentCaptor.capture());

        assertEquals(nextHearingDetailsFromHearings, nextHearingDetailsArgumentCaptor.getValue());
    }

    @Test
    public void should_set_next_hearing_date_from_hearings_on_cmr_updated() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(TRIGGER_CMR_UPDATED, CASE_REF, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));

        PartiesNotifiedResponse response =
            PartiesNotifiedResponse.builder()
                .responseReceivedDateTime(LocalDateTime.parse("2024-09-20T10:09:19"))
                .partiesNotified(LocalDateTime.parse("2024-09-20T10:09:19"))
                .requestVersion(1)
                .build();
        PartiesNotifiedResponses partiesNotifiedResponses =
            PartiesNotifiedResponses.builder()
                .responses(List.of(response))
                .hearingID(HEARING_ID).build();
        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);

        when(nextHearingDateService.enabled()).thenReturn(true);
        when(nextHearingDateService.getNextHearingDetails(Long.parseLong(CASE_REF)))
            .thenReturn(nextHearingDetailsFromHearings);

        listCmrHandler.handle(serviceData);

        verify(nextHearingDateService, times(1)).getNextHearingDetails(anyLong());
        verify(asylumCase).write(eq(NEXT_HEARING_DETAILS), nextHearingDetailsArgumentCaptor.capture());

        assertEquals(nextHearingDetailsFromHearings, nextHearingDetailsArgumentCaptor.getValue());
    }

    @Test
    public void should_set_next_hearing_date_from_case_data_when_calculating_from_hearings_fails() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(TRIGGER_CMR_UPDATED, CASE_REF, CASE_TYPE_ASYLUM))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.INTER)));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_ID, String.class))
            .thenReturn(Optional.of(HEARING_ID));

        PartiesNotifiedResponse response =
            PartiesNotifiedResponse.builder()
                .responseReceivedDateTime(LocalDateTime.parse("2024-09-20T10:09:19"))
                .partiesNotified(LocalDateTime.parse("2024-09-20T10:09:19"))
                .requestVersion(1)
                .build();
        PartiesNotifiedResponses partiesNotifiedResponses =
            PartiesNotifiedResponses.builder()
                .responses(List.of(response))
                .hearingID(HEARING_ID).build();
        when(hearingService.getPartiesNotified(HEARING_ID)).thenReturn(partiesNotifiedResponses);

        when(nextHearingDateService.enabled()).thenReturn(true);
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(nextHearingDateService.getNextHearingDetails(Long.parseLong(CASE_REF)))
            .thenThrow(new HmcException(new Throwable("Error")));

        listCmrHandler.handle(serviceData);

        verify(nextHearingDateService, times(1)).getNextHearingDetails(anyLong());
        verify(asylumCase).write(eq(NEXT_HEARING_DETAILS), nextHearingDetailsArgumentCaptor.capture());

        assertEquals(nextHearingDetailsFromCaseData, nextHearingDetailsArgumentCaptor.getValue());
    }
}
