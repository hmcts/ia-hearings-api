package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Component
@Slf4j
@RequiredArgsConstructor
public class DecisionAndReasonsStartedHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingService hearingService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.DECISION_AND_REASONS_STARTED;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        return new PreSubmitCallbackResponse<>(hearingService.createHearingWithPayload(callback));
    }
}