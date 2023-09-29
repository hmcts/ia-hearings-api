package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_REQUEST_VERSION_NUMBER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_RESPONSE_RECEIVED_DATE_TIME;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotified;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

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
        return true;
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String hearingId = serviceData.read(HEARING_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("HearingID can not be missing"));

        PartiesNotified payload = PartiesNotified.builder()
            .serviceData(serviceData)
            .build();

        long versionNumber = serviceData.read(HEARING_REQUEST_VERSION_NUMBER, Long.class)
            .orElseThrow(() -> new IllegalStateException("Hearing request version number can not be empty"));

        LocalDateTime receivedDateTime = serviceData.read(HEARING_RESPONSE_RECEIVED_DATE_TIME, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("Received date time can not be empty"));

        hearingService.updatePartiesNotified(hearingId, versionNumber, receivedDateTime, payload);

        return new ServiceDataResponse<>(serviceData);
    }

}
