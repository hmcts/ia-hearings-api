package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RE_LIST_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.ONPPRS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.CASE_MANAGEMENT_REVIEW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingChannel;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingType;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHmcStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelistHearingHandler extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isHmcStatus(serviceData, HmcStatus.AWAITING_LISTING)
            && !isHearingChannel(serviceData, ONPPRS)
            && (isHearingType(serviceData, CASE_MANAGEMENT_REVIEW) || isHearingType(serviceData, SUBSTANTIVE))
            && isReListing(serviceData);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);
        String hearingId = getHearingId(serviceData);

        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(RE_LIST_HEARING, caseId, CASE_TYPE_ASYLUM);
        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for case ID `{}` and hearing ID `{}`", RE_LIST_HEARING, caseId, hearingId);
        coreCaseDataService.triggerSubmitEvent(RE_LIST_HEARING, caseId, startEventResponse, asylumCase);

        return new ServiceDataResponse<>(serviceData);
    }

    private boolean isReListing(ServiceData serviceData) {
        String caseId = getCaseReference(serviceData);
        String hearingId = getHearingId(serviceData);

        AsylumCase asylumCase = coreCaseDataService.getCase(caseId);

        return asylumCase.read(CURRENT_HEARING_ID, String.class)
            .map(hearingId::equals)
            .orElse(false);
    }
}
