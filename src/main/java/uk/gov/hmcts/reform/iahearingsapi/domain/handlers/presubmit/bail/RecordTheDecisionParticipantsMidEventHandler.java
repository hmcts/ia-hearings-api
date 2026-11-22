package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.bail;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.HearingParticipantsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;

/**
 * Pre-fills the participants page of the record the decision event with the attendees HMC has for the chosen
 * hearing day.
 */
@Component
@RequiredArgsConstructor
public class RecordTheDecisionParticipantsMidEventHandler implements PreSubmitCallbackHandler<BailCase> {

    static final String TIMES_PAGE_ID = "timesRecordAttendeesAndDuration";

    private final HearingActualsService hearingActualsService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == Event.RECORD_THE_DECISION
               && Objects.equals(callback.getPageId(), TIMES_PAGE_ID);
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        bailCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)
            .filter(hearingId -> !hearingId.isBlank())
            .ifPresent(hearingId -> populateParticipants(bailCase, hearingId));

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private void populateParticipants(BailCase bailCase, String hearingId) {
        String selectedStartTime = bailCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class)
            .map(DynamicList::getValue)
            .map(Value::getCode)
            .orElse("");

        // keeps the attendees already recorded when the user comes back to this page for the same hearing day
        String source = hearingId + "|" + selectedStartTime;
        boolean alreadyPopulated = bailCase.read(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, String.class)
            .map(source::equals)
            .orElse(false);
        if (alreadyPopulated) {
            return;
        }

        bailCase.write(
            ATTENDEES_AND_DURATION_PARTICIPANTS,
            HearingParticipantsMapper.toParticipants(
                hearingActualsService.getHearingActuals(hearingId),
                selectedStartTime
            )
        );
        bailCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, source);
    }
}
