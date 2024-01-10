package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_CATEGORY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HANDLE_HEARING_EXCEPTION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHmcStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseCategoryModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@Slf4j
@Component
@RequiredArgsConstructor
public class HearingExceptionHandler implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        Optional<List<CaseCategoryModel>> maybeCaseCategory = serviceData.read(CASE_CATEGORY);
        List<CaseCategoryModel> caseCategory = maybeCaseCategory
            .orElseThrow(() -> new IllegalStateException("Case category can not be null"));
        boolean isBailsCase = caseCategory.stream().anyMatch(category -> category.getCategoryValue().contains("BLS"));
        return isHmcStatus(serviceData, HmcStatus.EXCEPTION) && !isBailsCase;
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = serviceData.read(CASE_REF, String.class)
            .orElseThrow(() -> new IllegalStateException("Case reference can not be null"));

        AsylumCase asylumCase = null;
        StartEventResponse startEventResponse = null;
        try {
            startEventResponse = coreCaseDataService.startCaseEvent(HANDLE_HEARING_EXCEPTION, caseId, CASE_TYPE_ASYLUM);
            asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);
        } catch (Exception e) {
            log.error("Cannot get case {} when trying to retrieve it to trigger handleHearingException."
                    + "core-case-data-api threw an exception with following message: {}",
                caseId,
                e.getMessage());
        }

        if (asylumCase != null) {
            coreCaseDataService.triggerSubmitEvent(HANDLE_HEARING_EXCEPTION, caseId, startEventResponse, asylumCase);
        }

        return new ServiceDataResponse<>(serviceData);
    }
}
