package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.END_APPEAL_OUTCOME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLATION_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLATION_SUBMITTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CANCELLED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.CLOSED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.EXCEPTION;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Component
@Slf4j
public class EndAppealRequestSubmitHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public static final String WITHDRAWN = "Withdrawn";
    public static final String ABANDONED = "Abandoned";
    HearingService hearingService;


    public EndAppealRequestSubmitHandler(
        HearingService hearingService
    ) {
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && Objects.equals(
            Event.END_APPEAL,
            callback.getEvent()
        );
    }

    /**
     * When appeal is ended, delete any hearings on the case that are not yet completed or cancelled.
     * Then calls HMC API to delete hearings, if any of the calls is unsuccessful then will set a CCD field to
     * indicate manual hearings cancellation is required.
     */
    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        asylumCase.write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private static boolean canBeCanceled(CaseHearing hearing) {
        return List.of(CLOSED, CANCELLED, CANCELLATION_REQUESTED, CANCELLATION_SUBMITTED, EXCEPTION)
            .stream()
            .noneMatch(status -> hearing.getHmcStatus().equals(status));
    }

    private static String mapToRefData(Optional<String> endAppealOutcome) {
        String cancellationReason = null;

        if (endAppealOutcome.isPresent()) {
            cancellationReason = switch (endAppealOutcome.get()) {
                case WITHDRAWN -> "withdraw";
                case ABANDONED -> "abandoned";
                default -> "n/a";
            };
        }
        return cancellationReason;
    }
}
