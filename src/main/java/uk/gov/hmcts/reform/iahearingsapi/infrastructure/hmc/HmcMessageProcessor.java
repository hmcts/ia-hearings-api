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
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMCTS_SERVICE_CODE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMC_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_CATEGORY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils.convertFromUTC;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingRequestDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HmcMessage;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Slf4j
@Service
@RequiredArgsConstructor
public class HmcMessageProcessor {

    private final HmcUpdateDispatcher<ServiceData> dispatcher;
    private final HearingService hearingService;

    public void processMessage(HmcMessage hmcMessage) {

        log.info(
            "Processing HMC hearing update message for Hearing ID `{}` and Case ID `{}` and HmcStatus `{}`",
            hmcMessage.getHearingId(), hmcMessage.getCaseId(), hmcMessage.getHearingUpdate().getHmcStatus());

        ServiceData serviceData = new ServiceData();

        HearingUpdate hearingUpdate = hmcMessage.getHearingUpdate();

        HmcStatus hmcStatus = hearingUpdate.getHmcStatus();
        serviceData.write(HMC_STATUS, hmcStatus);
        serviceData.write(CASE_REF, hmcMessage.getCaseId());
        serviceData.write(HMCTS_SERVICE_CODE, hmcMessage.getHmctsServiceCode());
        serviceData.write(HEARING_ID, hmcMessage.getHearingId());
        HearingGetResponse hearingGetResponse = hearingService.getHearing(hmcMessage.getHearingId());
        serviceData.write(CASE_CATEGORY, hearingGetResponse.getCaseDetails().getCaseCategories());
        /*
        This is only true if the strategy chosen to consume updates is real-time processing
        for messages with any HmcStatus from HmcHearingsEventTopicListener instead of batch-processing
        through UnNotifiedHearingsProcessor
         */
        if (!hmcStatus.equals(HmcStatus.EXCEPTION)) {
            if (hearingUpdate.getNextHearingDate() != null) {
                serviceData.write(NEXT_HEARING_DATE, convertFromUTC(hearingUpdate.getNextHearingDate()));
            }
            serviceData.write(HEARING_VENUE_ID, hearingUpdate.getHearingVenueId());
            serviceData.write(HEARING_LISTING_STATUS, hearingUpdate.getHearingListingStatus());
            serviceData.write(LIST_ASSIST_CASE_STATUS, hearingUpdate.getListAssistCaseStatus());
            serviceData.write(HEARING_RESPONSE_RECEIVED_DATE_TIME, hearingUpdate.getHearingResponseReceivedDateTime());
            addExtraDataFromHearingResponse(serviceData, hearingGetResponse);
        }

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
