package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class StateTest {
    @ParameterizedTest
    @MethodSource("provideStateAndExpectedString")
    void toString_returns_expected_string(State state, String expectedString) {
        assertEquals(expectedString, state.toString(),
                     String.format("Expected State.%s.toString() to return '%s'", state.name(), expectedString));
    }

    private static Stream<Arguments> provideStateAndExpectedString() {
        return Stream.of(
            Arguments.of(State.ADJOURNED, "adjourned"),
            Arguments.of(State.APPEAL_STARTED, "appealStarted"),
            Arguments.of(State.APPEAL_SUBMITTED, "appealSubmitted"),
            Arguments.of(State.APPEAL_SUBMITTED_OUT_OF_TIME, "appealSubmittedOutOfTime"),
            Arguments.of(State.APPEAL_TAKEN_OFFLINE, "appealTakenOffline"),
            Arguments.of(State.APPLICATION_ENDED, "applicationEnded"),
            Arguments.of(State.APPLICATION_STARTED, "applicationStarted"),
            Arguments.of(State.APPLICATION_STARTED_BY_HO, "applicationStartedByHO"),
            Arguments.of(State.APPLICATION_STARTED_BY_LR, "applicationStartedByLR"),
            Arguments.of(State.APPLICATION_SUBMITTED, "applicationSubmitted"),
            Arguments.of(State.AWAITING_CMA_REQUIREMENTS, "awaitingCmaRequirements"),
            Arguments.of(State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS, "awaitingClarifyingQuestionsAnswers"),
            Arguments.of(State.AWAITING_RESPONDENT_EVIDENCE, "awaitingRespondentEvidence"),
            Arguments.of(State.AWAITING_REASONS_FOR_APPEAL, "awaitingReasonsForAppeal"),
            Arguments.of(State.BAIL_SUMMARY_UPLOADED, "bailSummaryUploaded"),
            Arguments.of(State.CASE_BUILDING, "caseBuilding"),
            Arguments.of(State.CASE_UNDER_REVIEW, "caseUnderReview"),
            Arguments.of(State.CMA_ADJUSTMENTS_AGREED, "cmaAdjustmentsAgreed"),
            Arguments.of(State.CMA_LISTED, "cmaListed"),
            Arguments.of(State.CMA_REQUIREMENTS_SUBMITTED, "cmaRequirementsSubmitted"),
            Arguments.of(State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED, "clarifyingQuestionsAnswersSubmitted"),
            Arguments.of(State.DECIDED, "decided"),
            Arguments.of(State.DECISION, "decision"),
            Arguments.of(State.DECISION_CONDITIONAL_BAIL, "decisionConditionalBail"),
            Arguments.of(State.DECISION_DECIDED, "decisionDecided"),
            Arguments.of(State.ENDED, "ended"),
            Arguments.of(State.FINAL_BUNDLING, "finalBundling"),
            Arguments.of(State.FTPA_DECIDED, "ftpaDecided"),
            Arguments.of(State.FTPA_SUBMITTED, "ftpaSubmitted"),
            Arguments.of(State.HEARING_AND_OUTCOME, "hearingAndOutcome"),
            Arguments.of(State.LISTING, "listing"),
            Arguments.of(State.PENDING_PAYMENT, "pendingPayment"),
            Arguments.of(State.PRE_HEARING, "preHearing"),
            Arguments.of(State.PREPARE_FOR_HEARING, "prepareForHearing"),
            Arguments.of(State.REASONS_FOR_APPEAL_SUBMITTED, "reasonsForAppealSubmitted"),
            Arguments.of(State.RESPONDENT_REVIEW, "respondentReview"),
            Arguments.of(State.SUBMIT_HEARING_REQUIREMENTS, "submitHearingRequirements"),
            Arguments.of(State.UNSIGNED_DECISION, "unsignedDecision"),
            Arguments.of(State.UNKNOWN, "unknown")
        );
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(39, State.values().length);
    }
}
