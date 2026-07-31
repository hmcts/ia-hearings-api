package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubstantiveHearingListedHandler extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final LocationRefDataService locationRefDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        String caseId = getCaseReference(serviceData);

        CaseDetails caseDetails = coreCaseDataService.getCaseDetails(caseId);
        State caseState = State.get(caseDetails.getState());
        AsylumCase asylumCase = coreCaseDataService.mapCaseDetailsToAsylumCase(caseDetails);
        boolean isStf = asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)
            .orElse(YesOrNo.NO).equals(YesOrNo.YES);

        return isSubstantiveListedHearing(serviceData, isStf)
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

        boolean isAppealsLocationRefDataEnabled = HearingsUtils.isAppealsLocationRefDataEnabled(asylumCase);

        updateListCaseHearingDetails(serviceData, asylumCase, isAppealsLocationRefDataEnabled, caseId,
            locationRefDataService.getCourtVenuesAsServiceUser(),
            locationRefDataService.getHearingLocationsDynamicList(true));

        log.info("Sending `{}` event for  Case ID `{}`", LIST_CASE, caseId);
        coreCaseDataService.triggerSubmitEvent(LIST_CASE, caseId, startEventResponse, asylumCase);

        return new ServiceDataResponse<>(serviceData);
    }
}
