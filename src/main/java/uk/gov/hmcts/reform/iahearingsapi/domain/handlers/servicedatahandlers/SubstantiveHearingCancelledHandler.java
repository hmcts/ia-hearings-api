package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.NextHearingDateService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubstantiveHearingCancelledHandler extends ListedHearingService
    implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final NextHearingDateService nextHearingDateService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isSubstantiveCancelledHearing(serviceData);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);

        if (nextHearingDateService.enabled()) {
            log.info("Trigger hearing cancelled event for case ID " + caseId);
            coreCaseDataService.hearingCancelledTask(caseId);
        } else {
            log.info("Next hearing date not enabled for case {}", caseId);
        }

        return new ServiceDataResponse<>(serviceData);
    }
}
