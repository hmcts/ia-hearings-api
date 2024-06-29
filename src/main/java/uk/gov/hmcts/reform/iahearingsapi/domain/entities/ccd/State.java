package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum State {
    CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED("clarifyingQuestionsAnswersSubmitted"),

    ADJOURNED("adjourned"),
    APPEAL_STARTED("appealStarted"),
    APPEAL_SUBMITTED("appealSubmitted"),
    APPEAL_TAKEN_OFFLINE("appealTakenOffline"),
    APPEAL_SUBMITTED_OUT_OF_TIME("appealSubmittedOutOfTime"),
    APPLICATION_ENDED("applicationEnded"),
    APPLICATION_STARTED("applicationStarted"),
    APPLICATION_STARTED_BY_LR("applicationStartedByLR"),
    APPLICATION_STARTED_BY_HO("applicationStartedByHO"),
    APPLICATION_SUBMITTED("applicationSubmitted"),
    AWAITING_CMA_REQUIREMENTS("awaitingCmaRequirements"),
    AWAITING_CLARIFYING_QUESTIONS_ANSWERS("awaitingClarifyingQuestionsAnswers"),
    AWAITING_RESPONDENT_EVIDENCE("awaitingRespondentEvidence"),
    AWAITING_REASONS_FOR_APPEAL("awaitingReasonsForAppeal"),
    BAIL_SUMMARY_UPLOADED("bailSummaryUploaded"),
    CASE_BUILDING("caseBuilding"),
    CASE_UNDER_REVIEW("caseUnderReview"),
    CMA_ADJUSTMENTS_AGREED("cmaAdjustmentsAgreed"),
    CMA_LISTED("cmaListed"),
    CMA_REQUIREMENTS_SUBMITTED("cmaRequirementsSubmitted"),
    DECIDED("decided"),
    DECISION("decision"),
    DECISION_CONDITIONAL_BAIL("decisionConditionalBail"),
    DECISION_DECIDED("decisionDecided"),
    ENDED("ended"),
    FINAL_BUNDLING("finalBundling"),
    FTPA_SUBMITTED("ftpaSubmitted"),
    FTPA_DECIDED("ftpaDecided"),
    HEARING_AND_OUTCOME("hearingAndOutcome"),
    LISTING("listing"),
    PENDING_PAYMENT("pendingPayment"),
    PRE_HEARING("preHearing"),
    PREPARE_FOR_HEARING("prepareForHearing"),
    REASONS_FOR_APPEAL_SUBMITTED("reasonsForAppealSubmitted"),
    RESPONDENT_REVIEW("respondentReview"),
    SUBMIT_HEARING_REQUIREMENTS("submitHearingRequirements"),
    UNSIGNED_DECISION("unsignedDecision"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    State(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    private static final Map<String, State> lookup = new HashMap<>();

    static {
        for (State state : State.values()) {
            lookup.put(state.id, state);
        }
    }

    public static State get(String name) {
        return lookup.get(name);
    }

}
