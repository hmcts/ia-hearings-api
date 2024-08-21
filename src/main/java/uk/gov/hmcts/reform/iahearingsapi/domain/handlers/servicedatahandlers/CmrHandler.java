package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CMR_RE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import java.util.List;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmrHandler extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final HearingService hearingService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isCmrListedHearing(serviceData)
               || isCmrCancelledHearing(serviceData);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String hearingId = serviceData.read(HEARING_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("HearingID can not be missing"));
        String caseId = getCaseReference(serviceData);

        if (isCmrListedHearing(serviceData)) {
            PartiesNotifiedResponses partiesNotifiedResponses = hearingService.getPartiesNotified(hearingId);
            if (isInitialListing(hearingId, partiesNotifiedResponses.getResponses())) {
                handleCmrListing(caseId);
            } else {
                boolean cmrHearingUpdated = isCmrUpdated(serviceData, partiesNotifiedResponses.getResponses());
                if (cmrHearingUpdated) {
                    handleCmrReListing(caseId);
                    log.info("cmrRelistingHandler triggered for hearing " + hearingId);
                } else {
                    log.info("Hearing date, channel, duration and location not updated");
                    log.info("cmrRelistingHandler not triggered for hearing " + hearingId);
                }
            }
        } else {
            handleCmrReListing(caseId);
            log.info("cmrRelistingHandler triggered for hearing " + hearingId);
        }

        return new ServiceDataResponse<>(serviceData);
    }

    private void handleCmrListing(String caseId) {
        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(CMR_LISTING, caseId, CASE_TYPE_ASYLUM);

        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for  Case ID `{}`", CMR_LISTING, caseId);
        coreCaseDataService.triggerSubmitEvent(CMR_LISTING, caseId, startEventResponse, asylumCase);
    }

    private void handleCmrReListing(String caseId) {
        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(CMR_RE_LISTING, caseId, CASE_TYPE_ASYLUM);

        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for case ID `{}`", CMR_RE_LISTING, caseId);
        coreCaseDataService.triggerSubmitEvent(CMR_RE_LISTING, caseId, startEventResponse, asylumCase);
    }

    private boolean isCmrListedHearing(ServiceData serviceData) {
        return isCaseManagementReview(serviceData)
               && isListAssistCaseStatus(serviceData, ListAssistCaseStatus.LISTED);
    }

    private boolean isInitialListing(String hearingId, List<PartiesNotifiedResponse> partiesNotifiedResponses) {

        log.info("partiesNotifiedResponses for hearing " + hearingId + " : "
                 + partiesNotifiedResponses.toString());

        return partiesNotifiedResponses.isEmpty();
    }

    private boolean isCmrUpdated(
        ServiceData serviceData, List<PartiesNotifiedResponse> partiesNotifiedResponses) {
        Set<ServiceDataFieldDefinition> updatedTargetFields = findUpdatedServiceDataFields(
            serviceData, partiesNotifiedResponses, Set.of(
                NEXT_HEARING_DATE,
                HEARING_CHANNELS,
                DURATION,
                HEARING_VENUE_ID
            ));

        return !updatedTargetFields.isEmpty();
    }
}
