package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class EventTest {
    @ParameterizedTest
    @MethodSource("eventMapping")
    void has_correct_values(String expected, String actual) {
        assertEquals(expected, actual);
    }

    @Test
    void if_this_test_fails_it_is_because_eventMapping_needs_updating_with_your_changes() {
        List<String> eventMappingStrings = eventMapping().map(arg -> arg.get()[1])
            .map(String.class::cast)
            .toList();
        List<Event> missingEvents = Arrays.stream(Event.values())
            .filter(event -> !eventMappingStrings.contains(event.toString())).toList();
        assertTrue(missingEvents.isEmpty(), "The following events are missing from the eventMapping method: " + missingEvents);
    }

    static Stream<Arguments> eventMapping() {
        return Stream.of(
            Arguments.of("listCase", Event.LIST_CASE.toString()),
            Arguments.of("updateHearingRequest", Event.UPDATE_HEARING_REQUEST.toString()),
            Arguments.of("handleHearingException", Event.HANDLE_HEARING_EXCEPTION.toString()),
            Arguments.of("cmrReListing", Event.CMR_RE_LISTING.toString()),
            Arguments.of("recordAdjournmentDetails", Event.RECORD_ADJOURNMENT_DETAILS.toString()),
            Arguments.of("endAppeal", Event.END_APPEAL.toString()),
            Arguments.of("editCaseListing", Event.EDIT_CASE_LISTING.toString()),
            Arguments.of("startAppeal", Event.START_APPEAL.toString()),
            Arguments.of("editAppeal", Event.EDIT_APPEAL.toString()),
            Arguments.of("submitAppeal", Event.SUBMIT_APPEAL.toString()),
            Arguments.of("listCaseForFTOnly", Event.LIST_CASE_FOR_FT_ONLY.toString()),
            Arguments.of("updateInterpreterDetails", Event.UPDATE_INTERPRETER_DETAILS.toString()),
            Arguments.of("updateInterpreterBookingStatus", Event.UPDATE_INTERPRETER_BOOKING_STATUS.toString()),
            Arguments.of("cmrListing", Event.CMR_LISTING.toString()),
            Arguments.of("decideAnApplication", Event.DECIDE_AN_APPLICATION.toString()),
            Arguments.of("caseListing", Event.CASE_LISTING.toString()),
            Arguments.of("hearingCompletedOrCancelled", Event.HEARING_COMPLETED_OR_CANCELLED.toString()),
            Arguments.of("listCaseWithoutHearingRequirements", Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS.toString()),
            Arguments.of("decisionWithoutHearingListed", Event.DECISION_WITHOUT_HEARING_LISTED.toString()),
            Arguments.of("reviewHearingRequirements", Event.REVIEW_HEARING_REQUIREMENTS.toString()),
            Arguments.of("decisionAndReasonsStarted", Event.DECISION_AND_REASONS_STARTED.toString()),
            Arguments.of("restoreStateFromAdjourn", Event.RESTORE_STATE_FROM_ADJOURN.toString()),
            Arguments.of("UpdateNextHearingInfo", Event.UPDATE_NEXT_HEARING_INFO.toString()),
            Arguments.of("hearingCancelled", Event.HEARING_CANCELLED.toString()),
            Arguments.of("unknown", Event.UNKNOWN.toString())
        );
    }
}
