package uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_LISTING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_REQUEST_VERSION_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_RESPONSE_RECEIVED_DATE_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMC_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingRequestDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class HmcMessageProcessor {

    private final HmcMessageDispatcher<ServiceData> dispatcher;
    private final HearingService hearingService;
    private final ObjectMapper objectMapper;
    public void processMessage(HmcMessage hmcMessage) {

        log.info(
            "Processing HMC hearing update message for Hearing ID `{}` and Case ID `{}`",
            hmcMessage.getHearingId(), hmcMessage.getCaseId());

        ServiceData serviceData = objectMapper.convertValue(hmcMessage, new TypeReference<>(){});

        HearingUpdate hearingUpdate = hmcMessage.getHearingUpdate();
        serviceData.write(CASE_REF, hmcMessage.getCaseId());
        serviceData.write(HEARING_ID, hmcMessage.getHearingId());
        serviceData.write(NEXT_HEARING_DATE, hearingUpdate.getNextHearingDate());
        serviceData.write(HEARING_VENUE_ID, hearingUpdate.getHearingVenueId());
        serviceData.write(HMC_STATUS, hearingUpdate.getHmcStatus());
        serviceData.write(HEARING_LISTING_STATUS, hearingUpdate.getHearingListingStatus());
        serviceData.write(LIST_ASSIST_CASE_STATUS, hearingUpdate.getListAssistCaseStatus());
        serviceData.write(HEARING_RESPONSE_RECEIVED_DATE_TIME, hearingUpdate.getHearingResponseReceivedDateTime());

        HearingGetResponse hearingGetResponse = hearingService.getHearing(hmcMessage.getHearingId());
        addExtraDataFromHearingResponse(serviceData, hearingGetResponse);

        dispatcher.dispatch(serviceData);
    }

    private void addExtraDataFromHearingResponse(ServiceData serviceData, HearingGetResponse hearingGetResponse) {
        HearingDetails hearingDetails = hearingGetResponse.getHearingDetails();
        if (hearingDetails != null) {
            serviceData.write(HEARING_CHANNELS, hearingDetails.getHearingChannels());
            serviceData.write(HEARING_TYPE, hearingDetails.getHearingType());
            serviceData.write(DURATION, hearingDetails.getDuration());
        }

        HearingRequestDetails hearingRequestDetails = hearingGetResponse.getRequestDetails();
        if (hearingRequestDetails != null) {
            serviceData.write(HEARING_REQUEST_VERSION_NUMBER, hearingRequestDetails.getVersionNumber());
        }
    }
}
