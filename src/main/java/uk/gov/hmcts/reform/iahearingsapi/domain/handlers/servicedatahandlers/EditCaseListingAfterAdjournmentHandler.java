package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_ADJOURNMENT_DETAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AdjournmentDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EditCaseListingAfterAdjournmentHandler
    extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isSubstantiveListedHearing(serviceData)
               && adjournmentDetailsRecorded(getCaseReference(serviceData));
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);

        StartEventResponse startEventResponse = coreCaseDataService
            .startCaseEvent(EDIT_CASE_LISTING, caseId, CASE_TYPE_ASYLUM);
        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        updateListCaseHearingDetails(serviceData, asylumCase);

        log.info("Sending `{}` event for  Case ID `{}`", EDIT_CASE_LISTING, caseId);
        coreCaseDataService.triggerSubmitEvent(EDIT_CASE_LISTING, caseId, startEventResponse, asylumCase);

        return new ServiceDataResponse<>(serviceData);
    }

    private boolean adjournmentDetailsRecorded(String caseId) {
        AsylumCase asylumCase = coreCaseDataService.getCase(caseId);

        AdjournmentDetail adjournmentDetail = asylumCase
            .read(CURRENT_ADJOURNMENT_DETAIL, AdjournmentDetail.class).orElse(null);

        return adjournmentDetail != null && adjournmentDetail.getAdjournmentDetailsHearing() != null;
    }
}

