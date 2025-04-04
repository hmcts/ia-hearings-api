package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CMR_RE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.HEARING_CANCELLED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.UPDATE_NEXT_HEARING_INFO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NextHearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.NextHearingDateService;

@Slf4j
@Component
@RequiredArgsConstructor
public class NextHearingInfoHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final  NextHearingDateService nextHearingDateService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage,
                       "callbackStage must not be null");
        requireNonNull(callback,
                       "callback must not be null");

        return (callbackStage == ABOUT_TO_START && callback.getEvent() == UPDATE_NEXT_HEARING_INFO)
            || (callbackStage == ABOUT_TO_SUBMIT
            && List.of(LIST_CASE, EDIT_CASE_LISTING, CMR_LISTING, CMR_RE_LISTING, HEARING_CANCELLED)
            .contains(callback.getEvent()));
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        NextHearingDetails nextHearingDetails
            = nextHearingDateService.getNextHearingDetails(callback.getCaseDetails().getId());
        log.info("Next hearing date calculated for case ID {} hearing date {}, hearingID {}",
                 callback.getCaseDetails().getId(),
                 nextHearingDetails.getHearingDateTime(),
                 nextHearingDetails.getHearingId());
        asylumCase.write(
            NEXT_HEARING_DETAILS, nextHearingDetails);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
