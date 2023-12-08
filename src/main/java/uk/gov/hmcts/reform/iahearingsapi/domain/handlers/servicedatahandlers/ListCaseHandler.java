package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListCaseHandler extends SubstantiveListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        String caseId = getCaseReference(serviceData);
        State caseState = coreCaseDataService
            .getCaseState(caseId);

        return isSubstantiveListedHearing(serviceData)
            && isListAssistCaseStatus(serviceData, ListAssistCaseStatus.LISTED)
            && caseState.equals(LISTING);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);
        String caseType = coreCaseDataService.getCaseType(caseId);

        StartEventResponse startEventResponse = coreCaseDataService.startCaseEvent(LIST_CASE, caseId, caseType);

        switch (caseType) {
            case CASE_TYPE_ASYLUM:
                AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);
                updateListCaseHearingDetails(serviceData, asylumCase);
                log.info("Sending `{}` event for  Case ID `{}`", LIST_CASE, caseId);
                coreCaseDataService.triggerSubmitEvent(LIST_CASE, caseId, startEventResponse, asylumCase);
                break;
            case CASE_TYPE_BAIL:
                BailCase bailCase = coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse);
                updateListCaseHearingDetailsBail(serviceData, bailCase);
                log.info("Sending `{}` event for  Case ID `{}`", LIST_CASE, caseId);
                coreCaseDataService.triggerBailSubmitEvent(LIST_CASE, caseId, startEventResponse, bailCase);
                break;
            default:
                throw new IllegalStateException("Unknown case type");
        }

        return new ServiceDataResponse<>(serviceData);
    }
}

