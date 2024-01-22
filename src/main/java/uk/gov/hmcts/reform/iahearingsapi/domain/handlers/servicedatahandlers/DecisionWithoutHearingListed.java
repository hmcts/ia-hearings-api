package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DECISION_WITHOUT_HEARING_LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.ONPPRS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingChannel;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingType;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHmcStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Component
@RequiredArgsConstructor
public class DecisionWithoutHearingListed implements ServiceDataHandler<ServiceData> {

    public static final String CASE_TYPE_ASYLUM = "Asylum";

    private final CoreCaseDataService coreCaseDataService;
    private final HearingService hearingService;

    @Override
    public boolean canHandle(ServiceData serviceData) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isHearingType(serviceData, HearingType.SUBSTANTIVE)
               && isHearingChannel(serviceData, ONPPRS)
               && (isHmcStatus(serviceData, HmcStatus.LISTED)
                   || isHmcStatus(serviceData, HmcStatus.CANCELLATION_SUBMITTED));
    }

    @Override
    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {

        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String hearingId = serviceData.read(HEARING_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("HearingID can not be missing"));
        PartiesNotifiedResponses partiesNotifiedResponses = hearingService.getPartiesNotified(hearingId);

        boolean isFirstListingUpdate = partiesNotifiedResponses == null
                                              || partiesNotifiedResponses.getResponses().isEmpty();

        if (isFirstListingUpdate) {
            String caseId = serviceData.read(CASE_REF, String.class)
                .orElseThrow(() -> new IllegalStateException("Case reference can not be null"));

            StartEventResponse startEventResponse = coreCaseDataService.startCaseEvent(
                Event.DECISION_WITHOUT_HEARING_LISTED,
                caseId,
                CASE_TYPE_ASYLUM
            );

            AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

            asylumCase.write(DECISION_WITHOUT_HEARING_LISTED,
                             isHmcStatus(serviceData, HmcStatus.LISTED) ? YES : NO);

            coreCaseDataService.triggerSubmitEvent(
                Event.DECISION_WITHOUT_HEARING_LISTED,
                caseId,
                startEventResponse,
                asylumCase);
        }

        return new ServiceDataResponse<>(serviceData);
    }
}
