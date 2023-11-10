package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_REQUEST_VERSION_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_RESPONSE_RECEIVED_DATE_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHmcStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendServiceDataToHmcHandler implements ServiceDataHandler<ServiceData> {

    private final HearingService hearingService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");
        return !isHmcStatus(serviceData, HmcStatus.EXCEPTION);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String hearingId = serviceData.read(HEARING_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("HearingID can not be missing"));

        Optional<Long> versionNumber = serviceData.read(HEARING_REQUEST_VERSION_NUMBER, Long.class);

        Optional<LocalDateTime> receivedDateTime = serviceData.read(HEARING_RESPONSE_RECEIVED_DATE_TIME,
                                                                    LocalDateTime.class);

        if (versionNumber.isPresent() && receivedDateTime.isPresent()) {

            PartiesNotified payload = PartiesNotified.builder()
                .serviceData(serviceData)
                .build();

            hearingService.updatePartiesNotified(hearingId, versionNumber.get(), receivedDateTime.get(), payload);
        } else {
            log.info("Message received for hearing {} will not result in a partiesNotified update because both "
                     + "versionNumber and hearingResponseReceivedDateTime are necessary for the update.", hearingId);
            log.info("versionNumber: {} / hearingResponseReceivedDateTime: {}", versionNumber, receivedDateTime);
        }

        return new ServiceDataResponse<>(serviceData);
    }

}
