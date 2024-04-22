package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;

@Slf4j
@Component
public class BailListCaseHandler
    extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

    public BailListCaseHandler(LocationRefDataService locationRefDataService, CoreCaseDataService coreCaseDataService) {
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
        State caseState = coreCaseDataService.getCaseState(caseId);

        return isBailListedHearing(serviceData) && caseState.equals(APPLICATION_SUBMITTED);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);

        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(CASE_LISTING, caseId, CASE_TYPE_BAIL);
        BailCase bailCase = coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse);
        updateInitialBailCaseListing(serviceData, bailCase);
        log.info("Sending `{}` event for  Case ID `{}`", CASE_LISTING, caseId);
        coreCaseDataService.triggerBailSubmitEvent(CASE_LISTING, caseId,
                                                   startEventResponse, bailCase);

        return new ServiceDataResponse<>(serviceData);
    }
}

