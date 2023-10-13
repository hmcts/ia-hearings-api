package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HmcMessageProcessorTest {

    private static final String HMCTS_SERVICE_CODE = "BFA1";
    private static final long CASE_ID = 1111L;
    private static final String HEARING_ID = "2000000050";
    private static final LocalDateTime NEXT_HEARING_DATE = LocalDateTime.of(2023, 9, 29, 12, 0);
    private static final String HEARING_VENUE_ID = "1111";
    private static final HmcStatus HMC_STATUS_LISTED = HmcStatus.LISTED;
    private static final HmcStatus HMC_STATUS_EXCEPTION = HmcStatus.EXCEPTION;
    private static final ListingStatus HEARING_LISTING_STATUS = ListingStatus.FIXED;
    private static final ListAssistCaseStatus LIST_ASSIST_CASE_STATUS = ListAssistCaseStatus.LISTED;
    private static final LocalDateTime HEARING_RESPONSE_RECEIVED_DATE_TIME = LocalDateTime.of(2023, 9, 29, 11, 0);
    @Mock
    private HmcMessageDispatcher<ServiceData> dispatcher;
    @Mock
    private HearingService hearingService;
    @Mock
    private HmcMessage hmcMessage;
    @Mock
    private HearingUpdate hearingUpdate;
    @Mock
    private HearingGetResponse hearingGetResponse;
    @Mock
    private HearingDetails hearingDetails;

    private HmcMessageProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new HmcMessageProcessor(dispatcher, hearingService);
    }

    @Test
    void should_process_hmc_message_when_hmc_status_not_exception() {

        when(hmcMessage.getCaseId()).thenReturn(CASE_ID);
        when(hmcMessage.getHmctsServiceCode()).thenReturn(HMCTS_SERVICE_CODE);
        when(hmcMessage.getHearingId()).thenReturn(HEARING_ID);
        when(hmcMessage.getHearingUpdate()).thenReturn(hearingUpdate);
        when(hearingUpdate.getNextHearingDate()).thenReturn(NEXT_HEARING_DATE);
        when(hearingUpdate.getHearingVenueId()).thenReturn(HEARING_VENUE_ID);
        when(hearingUpdate.getHmcStatus()).thenReturn(HMC_STATUS_LISTED);
        when(hearingUpdate.getHearingListingStatus()).thenReturn(HEARING_LISTING_STATUS);
        when(hearingUpdate.getListAssistCaseStatus()).thenReturn(LIST_ASSIST_CASE_STATUS);
        when(hearingUpdate.getHearingResponseReceivedDateTime()).thenReturn(HEARING_RESPONSE_RECEIVED_DATE_TIME);
        when(hearingService.getHearing(HEARING_ID)).thenReturn(hearingGetResponse);
        when(hearingGetResponse.getHearingDetails()).thenReturn(hearingDetails);
        when(hearingDetails.getHearingChannels()).thenReturn(List.of("INTER"));
        when(hearingDetails.getHearingType()).thenReturn("SUBSTANTIVE");
        when(hearingDetails.getDuration()).thenReturn(150);

        processor.processMessage(hmcMessage);

        ServiceData serviceData = new ServiceData();
        serviceData.write(ServiceDataFieldDefinition.HMCTS_SERVICE_CODE, HMCTS_SERVICE_CODE);
        serviceData.write(CASE_REF, CASE_ID);
        serviceData.write(ServiceDataFieldDefinition.HEARING_ID, HEARING_ID);
        serviceData.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, NEXT_HEARING_DATE);
        serviceData.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, HEARING_VENUE_ID);
        serviceData.write(ServiceDataFieldDefinition.HMC_STATUS, HMC_STATUS_LISTED);
        serviceData.write(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, HEARING_LISTING_STATUS);
        serviceData.write(ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS, LIST_ASSIST_CASE_STATUS);
        serviceData.write(ServiceDataFieldDefinition.HEARING_RESPONSE_RECEIVED_DATE_TIME,
                                  HEARING_RESPONSE_RECEIVED_DATE_TIME);
        serviceData.write(HEARING_CHANNELS, List.of(HearingChannel.INTER));
        serviceData.write(HEARING_TYPE, HearingType.SUBSTANTIVE.getKey());
        serviceData.write(DURATION, 150);

        ArgumentCaptor<ServiceData> serviceDataArgumentCaptor = ArgumentCaptor.forClass(ServiceData.class);
        verify(dispatcher, times(1)).dispatch(serviceDataArgumentCaptor.capture());

        ServiceData argument = serviceDataArgumentCaptor.getValue();
        assertTrue(argument.keySet().containsAll(Set.of(ServiceDataFieldDefinition.HMCTS_SERVICE_CODE.value(),
                                             ServiceDataFieldDefinition.HEARING_ID.value(),
                                             ServiceDataFieldDefinition.NEXT_HEARING_DATE.value(),
                                             ServiceDataFieldDefinition.HEARING_VENUE_ID.value(),
                                             ServiceDataFieldDefinition.HMC_STATUS.value(),
                                             ServiceDataFieldDefinition.HEARING_LISTING_STATUS.value(),
                                             ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS.value(),
                                             ServiceDataFieldDefinition.HEARING_RESPONSE_RECEIVED_DATE_TIME.value(),
                                             HEARING_CHANNELS.value(),
                                             HEARING_TYPE.value(),
                                             DURATION.value())));
    }

    @Test
    void should_process_hmc_message_when_hmc_status_exception() {

        when(hmcMessage.getCaseId()).thenReturn(CASE_ID);
        when(hmcMessage.getHmctsServiceCode()).thenReturn(HMCTS_SERVICE_CODE);
        when(hmcMessage.getHearingId()).thenReturn(HEARING_ID);
        when(hmcMessage.getHearingUpdate()).thenReturn(hearingUpdate);
        when(hearingUpdate.getHmcStatus()).thenReturn(HMC_STATUS_EXCEPTION);

        processor.processMessage(hmcMessage);

        ServiceData serviceData = new ServiceData();
        serviceData.write(ServiceDataFieldDefinition.HMCTS_SERVICE_CODE, HMCTS_SERVICE_CODE);
        serviceData.write(CASE_REF, CASE_ID);
        serviceData.write(ServiceDataFieldDefinition.HEARING_ID, HEARING_ID);
        serviceData.write(ServiceDataFieldDefinition.HMC_STATUS, HMC_STATUS_EXCEPTION);

        ArgumentCaptor<ServiceData> serviceDataArgumentCaptor = ArgumentCaptor.forClass(ServiceData.class);
        verify(dispatcher, times(1)).dispatch(serviceDataArgumentCaptor.capture());

        ServiceData argument = serviceDataArgumentCaptor.getValue();
        assertTrue(argument.keySet().containsAll(Set.of(ServiceDataFieldDefinition.HMCTS_SERVICE_CODE.value(),
                                                        ServiceDataFieldDefinition.HEARING_ID.value(),
                                                        ServiceDataFieldDefinition.HMC_STATUS.value(),
                                                        CASE_REF.value())));
    }

}
