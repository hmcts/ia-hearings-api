package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.NextHearingDateService;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelledHearingHandler extends ListedHearingService
    implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final NextHearingDateService nextHearingDateService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isSubstantiveCancelledHearing(serviceData);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);

        log.info("CancelledHearingHandler triggered for case " + caseId);
        coreCaseDataService.triggerReviewInterpreterBookingTask(caseId);
        handleCancelledHearing(serviceData, caseId);

        if (nextHearingDateService.enabled()) {
            log.info("Trigger set next hearing date event for case " + caseId);
            coreCaseDataService.setNextHearingDate(caseId);
        }

        return new ServiceDataResponse<>(serviceData);
    }

    protected void handleCancelledHearing(ServiceData serviceData, String caseId) {
        String cancelledHearingId = serviceData.read(
            ServiceDataFieldDefinition.HEARING_ID, String.class).orElse("");

        AsylumCase asylumcase = coreCaseDataService.getCase(caseId);

        String currentHearingId = asylumcase.read(NEXT_HEARING_DETAILS, NextHearingDetails.class)
            .map(NextHearingDetails::getHearingId).orElse("");

        if (Objects.equals(currentHearingId, cancelledHearingId)) {
            coreCaseDataService.hearingCancelledTask(caseId);
        }
    }

}
