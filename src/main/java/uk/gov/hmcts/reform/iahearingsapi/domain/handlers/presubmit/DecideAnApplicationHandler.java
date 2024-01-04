package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MAKE_AN_APPLICATION_DECISION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.DECIDE_AN_APPLICATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.HEARING_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.LISTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.UPDATE_REQUESTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus.UPDATE_SUBMITTED;

import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@Component
@Slf4j
public class DecideAnApplicationHandler  implements PreSubmitCallbackHandler<AsylumCase> {

    HearingService hearingService;


    public DecideAnApplicationHandler(HearingService hearingService) {
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_SUBMIT && Objects.equals(DECIDE_AN_APPLICATION, callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String cancellationReason = asylumCase.read(MAKE_AN_APPLICATION_DECISION_REASON, String.class).orElse("");

        try {
            HearingsGetResponse hearings = hearingService.getHearings(callback.getCaseDetails().getId());
            hearings.getCaseHearings()
                .stream()
                .filter(DecideAnApplicationHandler::canBeCanceled)
                .forEach(
                    hearing -> {
                        hearingService
                            .deleteHearing(Long.valueOf(hearing.getHearingRequestId()), cancellationReason)
                            .getStatusCode();
                    });
            asylumCase.write(MANUAL_CANCEL_HEARINGS_REQUIRED, NO);
        } catch (HmcException e) {
            asylumCase.write(MANUAL_CANCEL_HEARINGS_REQUIRED, YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private static boolean canBeCanceled(CaseHearing hearing) {
        return Stream.of(HEARING_REQUESTED,
                         AWAITING_LISTING,
                         LISTED,
                         UPDATE_REQUESTED,
                         UPDATE_SUBMITTED).anyMatch(status -> hearing.getHmcStatus().equals(status))
               && Objects.equals(hearing.getHearingType(), HearingType.SUBSTANTIVE.getKey());
    }

}
