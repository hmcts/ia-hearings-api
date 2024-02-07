package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.DECISION_AND_REASONS_STARTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RESTORE_STATE_FROM_ADJOURN;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.REVIEW_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

@Component
@Slf4j
@RequiredArgsConstructor
public class AutoRequestHearingHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingService hearingService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        List<Event> targetEvents = List.of(
            LIST_CASE_WITHOUT_HEARING_REQUIREMENTS,
            DECISION_AND_REASONS_STARTED,
            REVIEW_HEARING_REQUIREMENTS,
            RESTORE_STATE_FROM_ADJOURN);

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (targetEvents.contains(callback.getEvent()) || autoRequestHearingForRecordAdjournmentEvent(callback));
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        hearingService.createHearingWithPayload(callback);

        return new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
    }

    private boolean autoRequestHearingForRecordAdjournmentEvent(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean isRecordAdjournmentDetailsEvent = callback.getEvent() == RECORD_ADJOURNMENT_DETAILS;

        boolean relistImmediately = asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
            .map(relist -> YES == relist)
            .orElseThrow(() -> new IllegalStateException("Response to relist case immediately is not present"));

        boolean adjournOnHearingDay = asylumCase
            .read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class)
            .map(hearingAdjournmentDay -> ON_HEARING_DATE == hearingAdjournmentDay)
            .orElseThrow(() -> new IllegalStateException("'Hearing adjournment when' is not present"));

        return isRecordAdjournmentDetailsEvent && relistImmediately && adjournOnHearingDay;

    }
}
