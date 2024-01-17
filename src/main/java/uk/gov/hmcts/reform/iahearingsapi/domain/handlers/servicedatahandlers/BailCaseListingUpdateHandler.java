package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ListingEvent.RELISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BailCaseListingUpdateHandler
    extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final HearingService hearingService;

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        String caseId = getCaseReference(serviceData);
        State caseState = coreCaseDataService
            .getCaseState(caseId);

        return isBailListedHearing(serviceData)
               && !caseState.equals(APPLICATION_SUBMITTED);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String hearingId = serviceData.read(HEARING_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("Hearing ID missing from serviceData"));

        List<PartiesNotifiedResponse> partiesNotifiedResponses = hearingService.getPartiesNotified(hearingId)
            .getResponses();

        boolean shouldTriggerCaseListing = false;

        if (!partiesNotifiedResponses.isEmpty()) {
            ServiceData previousServiceData = partiesNotifiedResponses.get(partiesNotifiedResponses.size() - 1)
                .getServiceData();

            Map<ServiceDataFieldDefinition,Object> updatedFieldValues = updatedFieldValues(
                serviceData,
                previousServiceData,
                Set.of(NEXT_HEARING_DATE,
                       HEARING_CHANNELS,
                       DURATION,
                       HEARING_VENUE_ID
                ));

            if (!updatedFieldValues.isEmpty()) {

                String caseId = getCaseReference(serviceData);

                StartEventResponse startEventResponse =
                    coreCaseDataService.startCaseEvent(CASE_LISTING, caseId, CASE_TYPE_BAIL);

                BailCase bailCase = coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse);
                bailCase.write(LISTING_EVENT, RELISTING);

                log.info("Sending `{}` event for Case ID `{}` (`{}`)", CASE_LISTING, caseId, RELISTING.getValue());
                coreCaseDataService.triggerBailSubmitEvent(CASE_LISTING, caseId, startEventResponse, bailCase);
            }
        }

        return new ServiceDataResponse<>(serviceData);
    }

    private Map<ServiceDataFieldDefinition,Object> updatedFieldValues(ServiceData latest, ServiceData previous,
                                                                      Set<ServiceDataFieldDefinition> fieldsToCompare) {

        Map<ServiceDataFieldDefinition,Object> updatedFieldValues = new HashMap<>();

        fieldsToCompare
            .forEach(field -> {
                Object latestValue = latest.read(field).orElse(null);
                Object previousValue = previous.read(field).orElse(null);
                if (!Objects.equals(latestValue, previousValue)) {
                    updatedFieldValues.put(field, latestValue);
                }
            });

        return updatedFieldValues;
    }

}
