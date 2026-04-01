package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_LANGUAGE_CATEGORY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SIGN_LANGUAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_INTERPRETER_SPOKEN_LANGUAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_INTERPRETER_SERVICES_NEEDED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HEARING_CANCELLED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.NextHearingDateService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubstantiveHearingCancelledHandler extends ListedHearingService
    implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final NextHearingDateService nextHearingDateService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isSubstantiveCancelledHearing(serviceData);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);

        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(HEARING_CANCELLED, caseId, CASE_TYPE_ASYLUM);
        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);
        if (nextHearingDateService.enabled()) {
            log.info("Trigger hearing cancelled event for case ID " + caseId);
            coreCaseDataService.hearingCancelledTask(caseId);

            boolean isInterpreterNeeded = isInterpreterNeeded(asylumCase);
            if (isInterpreterNeeded) {
                asylumCase.write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
                log.info("Setting trigger review interpreter task flag for hearing for case {}", caseId);
            } else {
                asylumCase.clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
            }
        } else {
            log.info("Next hearing date not enabled for case {}", caseId);
            asylumCase.clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
        }

        return new ServiceDataResponse<>(serviceData);
    }

    private boolean isInterpreterNeeded(AsylumCase asylumCase) {
        boolean isInterpreterServicesNeeded = asylumCase
            .read(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.class)
            .map(yesOrNo -> Objects.equals(yesOrNo, YES))
            .orElse(false);

        if (isInterpreterServicesNeeded) {
            Optional<List<String>> languageCategoriesOptional = asylumCase
                .read(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY);
            if (languageCategoriesOptional.isPresent()) {
                Optional<InterpreterLanguageRefData> appellantInterpreterSpokenLanguage =
                    asylumCase.read(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE);
                Optional<InterpreterLanguageRefData> appellantInterpreterSignLanguage =
                    asylumCase.read(APPELLANT_INTERPRETER_SIGN_LANGUAGE);

                return appellantInterpreterSpokenLanguage.isPresent()
                    && appellantInterpreterSpokenLanguage.get().getLanguageRefData() != null
                    || appellantInterpreterSignLanguage.isPresent()
                    && appellantInterpreterSignLanguage.get().getLanguageRefData() != null;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
