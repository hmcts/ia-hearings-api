package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;
import lombok.RequiredArgsConstructor;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelledHearingHandler extends SubstantiveListedHearingService
    implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

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
        triggerReviewInterpreterBookingTask(caseId);

        return new ServiceDataResponse<>(serviceData);
    }

    private void triggerReviewInterpreterBookingTask(String caseId) {
        StartEventResponse startEventResponse = coreCaseDataService.startCaseEvent(
            TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, caseId, CASE_TYPE_ASYLUM);

        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for  Case ID `{}`", TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, caseId);
        coreCaseDataService.triggerSubmitEvent(
            TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, caseId, startEventResponse, asylumCase);
    }
}
