package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ReasonCodes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;

import static java.util.Objects.requireNonNull;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateInterpreterDetailsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingService hearingService;
    private final UpdateHearingPayloadService updateHearingPayloadService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.UPDATE_INTERPRETER_DETAILS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        log.info("Updating interpreter details for case {}", callback.getCaseDetails().getId());
        try {
            HearingsGetResponse hearings = hearingService.getHearings(callback.getCaseDetails().getId());
            CaseHearing latestSubstantiveHearing =
                hearings.getCaseHearings()
                    .stream()
                    .filter(hearing -> hearing.getHearingType().equals(HearingType.SUBSTANTIVE.getKey()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No Substantive hearing was found."));

            log.info("Updating hearing details for hearing {}", latestSubstantiveHearing.getHearingRequestId());
            hearingService.updateHearing(
                updateHearingPayloadService.createUpdateHearingPayload(
                    asylumCase,
                    latestSubstantiveHearing.getHearingRequestId(),
                    ReasonCodes.OTHER.toString(),
                    false,
                    null
                ),
                latestSubstantiveHearing.getHearingRequestId()
            );
        } catch (Exception ex) {
            String errorMessage = String.format("Hearing cannot be auto updated for Case %s due to: %s",
                                                callback.getCaseDetails().getId(), ex.getMessage()
            );
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
