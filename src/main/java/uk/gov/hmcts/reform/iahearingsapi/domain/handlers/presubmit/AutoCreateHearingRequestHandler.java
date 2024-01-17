package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.CreateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CreateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoCreateHearingRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingService hearingService;
    private final CreateHearingPayloadService createHearingPayloadService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && List.of(LIST_CASE_WITHOUT_HEARING_REQUIREMENTS, REVIEW_HEARING_REQUIREMENTS)
                   .contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        log.info("Handling {} and creating new hearing for case {}",
                 callback.getEvent().toString(), callback.getCaseDetails().getId());

        try {
            CreateHearingRequest hmcHearingRequestPayload = createHearingPayloadService
                .buildCreateHearingRequest(callback.getCaseDetails());

            log.info("Sending request to HMC to create a hearing for case {}", callback.getCaseDetails().getId());
            hearingService.createHearing(hmcHearingRequestPayload);

        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new IllegalStateException(ex.getMessage());
        }

        asylumCase.write(MANUAL_CREATE_HEARINGS_REQUIRED, NO);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
