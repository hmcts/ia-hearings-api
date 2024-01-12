package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

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

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.SEND_UPLOAD_BAIL_SUMMARY_DIRECTION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class BailUploadSummaryDirectionHandler
    extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isBailListedHearing(serviceData);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);

        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(SEND_UPLOAD_BAIL_SUMMARY_DIRECTION, caseId, CASE_TYPE_BAIL);
        BailCase bailCase = coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse);
        updateListCaseSendHomeOfficeDirection(serviceData, bailCase);
        log.info("Sending `{}` event for  Case ID `{}`", SEND_UPLOAD_BAIL_SUMMARY_DIRECTION, caseId);
        coreCaseDataService.triggerBailSubmitEvent(SEND_UPLOAD_BAIL_SUMMARY_DIRECTION, caseId,
                                                   startEventResponse, bailCase);

        return new ServiceDataResponse<>(serviceData);
    }
}

