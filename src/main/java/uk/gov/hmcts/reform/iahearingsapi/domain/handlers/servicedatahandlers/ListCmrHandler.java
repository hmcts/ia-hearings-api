package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.TRIGGER_CMR_LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.TRIGGER_CMR_UPDATED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListCmrHandler extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final HearingService hearingService;

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

        String hearingId = serviceData.read(HEARING_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("HearingID can not be missing"));
        log.info("ListCmrHandler triggered for hearing " + hearingId);
        PartiesNotifiedResponses partiesNotifiedResponses = hearingService.getPartiesNotified(hearingId);
        log.info("partiesNotifiedResponses for hearing " + hearingId + " : "
            + partiesNotifiedResponses.getResponses().toString());
        String caseId = getCaseReference(serviceData);
        if (partiesNotifiedResponses.getResponses().isEmpty()) {
            triggerCmrListedNotification(caseId);
        } else {
            Set<ServiceDataFieldDefinition> updatedTargetFields = findUpdatedServiceDataFields(
                serviceData, partiesNotifiedResponses.getResponses(), Set.of(
                    NEXT_HEARING_DATE,
                    HEARING_CHANNELS,
                    DURATION,
                    HEARING_VENUE_ID
                ));

            if (!updatedTargetFields.isEmpty()) {
                triggerCmrUpdatedNotification(caseId);
            }
        }

        return new ServiceDataResponse<>(serviceData);
    }

    private void triggerCmrListedNotification(String caseId) {
        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(TRIGGER_CMR_LISTED, caseId, CASE_TYPE_ASYLUM);

        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for  Case ID `{}`", TRIGGER_CMR_LISTED, caseId);
        coreCaseDataService.triggerSubmitEvent(TRIGGER_CMR_LISTED, caseId, startEventResponse, asylumCase);
    }

    private void triggerCmrUpdatedNotification(String caseId) {
        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(TRIGGER_CMR_UPDATED, caseId, CASE_TYPE_ASYLUM);

        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for case ID `{}`", TRIGGER_CMR_UPDATED, caseId);
        coreCaseDataService.triggerSubmitEvent(TRIGGER_CMR_UPDATED, caseId, startEventResponse, asylumCase);
    }
}
