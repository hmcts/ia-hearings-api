package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.bail;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;

import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.HearingsToDynamicListMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

/**
 * Lists the bail hearings HMC is awaiting actuals for, so the hearing details can be recorded with the decision.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RecordTheDecisionHearingsPreparer implements PreSubmitCallbackHandler<BailCase> {

    private final HearingService hearingService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.RECORD_THE_DECISION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        HearingsGetResponse hearings = hearingService.getHearings(callback.getCaseDetails().getId());
        BailCase bailCase = callback.getCaseDetails().getCaseData();

        bailCase.write(
            ATTENDEES_AND_DURATION_HEARING,
            HearingsToDynamicListMapper.map(hearings, Set.of(HmcStatus.AWAITING_ACTUALS), HearingType.BAIL)
        );

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
