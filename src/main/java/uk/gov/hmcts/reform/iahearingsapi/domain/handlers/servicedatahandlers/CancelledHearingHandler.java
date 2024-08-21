package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.NextHearingDateService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelledHearingHandler extends ListedHearingService
    implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final NextHearingDateService nextHearingDateService;

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

        if (nextHearingDateService.enabled()) {
            if (isNextHearingCancelled(serviceData, caseId)) {
                //If ID of cancelled hearing is the same as the hearing ID in the next hearing date details
                log.info("Reset next hearing info for case " + caseId);
                coreCaseDataService.hearingCancelledTask(caseId);
            } else {
                log.info("Update next hearing info for case " + caseId);
                coreCaseDataService.updateNextHearingInfo(caseId);
            }
        } else {
            log.info("Next hearing date not enabled for case {}", caseId);
        }

        return new ServiceDataResponse<>(serviceData);
    }

    private boolean isNextHearingCancelled(ServiceData serviceData, String caseId) {
        String cancelledHearingId = serviceData.read(
            ServiceDataFieldDefinition.HEARING_ID, String.class).orElse("");

        AsylumCase asylumCase = coreCaseDataService.getCase(caseId);

        String currentHearingId = asylumCase.read(NEXT_HEARING_DETAILS, NextHearingDetails.class)
            .map(NextHearingDetails::getHearingId).orElse("");

        return Objects.equals(currentHearingId, cancelledHearingId);
    }
}
