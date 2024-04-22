package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;

@Slf4j
@Component
public class ListCaseHandler extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

    public ListCaseHandler(LocationRefDataService locationRefDataService, CoreCaseDataService coreCaseDataService) {
        super(locationRefDataService);
        this.coreCaseDataService = coreCaseDataService;
    }

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

        StartEventResponse startEventResponse = coreCaseDataService.startCaseEvent(LIST_CASE, caseId, CASE_TYPE_ASYLUM);
        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        updateListCaseHearingDetails(serviceData, asylumCase);

        log.info("Sending `{}` event for  Case ID `{}`", LIST_CASE, caseId);
        coreCaseDataService.triggerSubmitEvent(LIST_CASE, caseId, startEventResponse, asylumCase);

        return new ServiceDataResponse<>(serviceData);
    }
}

