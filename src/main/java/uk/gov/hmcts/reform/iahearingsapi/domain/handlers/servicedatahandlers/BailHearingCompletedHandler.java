package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HEARING_COMPLETED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BailHearingCompletedHandler
    extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData) {
        requireNonNull(serviceData, "serviceData must not be null");
        return isBailHearingCompleted(serviceData);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);
        log.info("BailListCaseHandler called for  Case ID `{}`", caseId);

        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(HEARING_COMPLETED, caseId, CASE_TYPE_BAIL);
        BailCase bailCase = coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for  Case ID `{}`", HEARING_COMPLETED, caseId);
        coreCaseDataService.triggerBailSubmitEvent(HEARING_COMPLETED, caseId,
                                                   startEventResponse, bailCase);
        log.info("Completed `{}` event for  Case ID `{}`", HEARING_COMPLETED, caseId);
        return new ServiceDataResponse<>(serviceData);
    }
}

