package uk.gov.hmcts.reform.iahearingsapi.domain.service;

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

import java.time.LocalDateTime;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingDaySchedule;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UnNotifiedHearingsResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.hmc.HmcUpdateDispatcher;

@Slf4j
@Component
public class UnNotifiedHearingsProcessor implements Runnable {

    private HmcUpdateDispatcher<ServiceData> dispatcher;
    private HearingService hearingService;
    private long batchIntervalSizeInMinutes;

    public UnNotifiedHearingsProcessor(HmcUpdateDispatcher<ServiceData> dispatcher,
                                       HearingService hearingService,
                                       @Value("${hearingValues.batchIntervalSizeInMinutes}")
                                           long batchIntervalSizeInMinutes) {
        this.dispatcher = dispatcher;
        this.hearingService = hearingService;
        this.batchIntervalSizeInMinutes = batchIntervalSizeInMinutes;
    }

    @Override
    public void run() {
        //LocalDateTime intervalEnd = LocalDateTime.now();
        //LocalDateTime intervalStart = intervalEnd.minusMinutes(batchIntervalSizeInMinutes);
        log.info("Running UnNotifiedHearingsProcessor task to retrieve unNotifiedHearings");
        processUnNotifiedHearings();
    }

    public void processUnNotifiedHearings() {

        UnNotifiedHearingsResponse unNotifiedHearings = hearingService.getUnNotifiedHearings(LocalDateTime.now());

        ServiceData serviceData = new ServiceData();

        unNotifiedHearings.getHearingIds().forEach(unNotifiedHearingId -> {

            PartiesNotifiedResponses partiesNotifiedResponses = hearingService.getPartiesNotified(unNotifiedHearingId);
            partiesNotifiedResponses.getResponses().stream()
                .max(Comparator.comparing(PartiesNotifiedResponse::getRequestVersion))
                .ifPresent(latestResponse -> {

                    HearingGetResponse hearing = hearingService.getHearing(unNotifiedHearingId);
                    serviceData.write(HMC_STATUS, HmcStatus.valueOf(hearing.getRequestDetails().getStatus()));
                    serviceData.write(CASE_REF, hearing.getCaseDetails().getCaseRef());
                    serviceData.write(HMCTS_SERVICE_CODE, hearing.getCaseDetails().getHmctsServiceCode());
                    serviceData.write(HEARING_ID, unNotifiedHearingId);

                    HearingDaySchedule hearingDaySchedule = hearing.getHearingResponse().getHearingDaySchedule()
                        .stream().min(Comparator.comparing(HearingDaySchedule::getHearingStartDateTime))
                        .orElse(null);

                    if (null != hearingDaySchedule) {
                        serviceData.write(NEXT_HEARING_DATE, hearingDaySchedule.getHearingStartDateTime());
                        serviceData.write(HEARING_VENUE_ID, hearingDaySchedule.getHearingVenueId());
                    }

                    serviceData.write(HEARING_LISTING_STATUS, hearing.getHearingResponse().getListingStatus());
                    serviceData.write(LIST_ASSIST_CASE_STATUS, hearing.getHearingResponse().getLaCaseStatus());
                    serviceData.write(HEARING_RESPONSE_RECEIVED_DATE_TIME, hearing.getHearingResponse()
                        .getReceivedDateTime());

                    serviceData.write(HEARING_CHANNELS, hearing.getHearingDetails().getHearingChannels());
                    serviceData.write(HEARING_TYPE, hearing.getHearingDetails().getHearingType());
                    serviceData.write(DURATION, hearing.getHearingDetails().getDuration());
                    serviceData.write(HEARING_REQUEST_VERSION_NUMBER, hearing.getRequestDetails().getVersionNumber());
                });

            dispatcher.dispatch(serviceData);
        });
    }

}
