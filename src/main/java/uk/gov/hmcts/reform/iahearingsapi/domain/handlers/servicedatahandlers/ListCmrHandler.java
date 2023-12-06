package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_CMR_NOTIFICATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListCmrHandler extends SubstantiveListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isCaseManagementReview(serviceData)
            && isListAssistCaseStatus(serviceData, ListAssistCaseStatus.LISTED);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);

        StartEventResponse startEventResponse = coreCaseDataService.startCaseEvent(UPDATE_CMR_NOTIFICATION, caseId);
        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);


        log.info("Sending `{}` event for  Case ID `{}`", UPDATE_CMR_NOTIFICATION, caseId);
        coreCaseDataService.triggerSubmitEvent(UPDATE_CMR_NOTIFICATION, caseId, startEventResponse, asylumCase);

        return new ServiceDataResponse<>(serviceData);
    }
}