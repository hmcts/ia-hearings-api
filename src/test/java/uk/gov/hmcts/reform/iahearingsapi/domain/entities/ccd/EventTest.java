package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EventTest {

    @Test
    void has_correct_values() {
        assertEquals("listCase", Event.LIST_CASE.toString());
        assertEquals("updateHearingRequest", Event.UPDATE_HEARING_REQUEST.toString());
        assertEquals("handleHearingException", Event.HANDLE_HEARING_EXCEPTION.toString());
        assertEquals("recordAdjournmentDetails", Event.RECORD_ADJOURNMENT_DETAILS.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
        assertEquals("endAppeal", Event.END_APPEAL.toString());
        assertEquals("editCaseListing", Event.EDIT_CASE_LISTING.toString());
        assertEquals("startAppeal", Event.START_APPEAL.toString());
        assertEquals("editAppeal", Event.EDIT_APPEAL.toString());
        assertEquals("submitAppeal", Event.SUBMIT_APPEAL.toString());
        assertEquals("listCaseForFTOnly", Event.LIST_CASE_FOR_FT_ONLY.toString());
        assertEquals("updateInterpreterDetails", Event.UPDATE_INTERPRETER_DETAILS.toString());
        assertEquals("updateInterpreterBookingStatus", Event.UPDATE_INTERPRETER_BOOKING_STATUS.toString());
        assertEquals("updateCmrNotification", Event.UPDATE_CMR_NOTIFICATION.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(14, Event.values().length);
    }
}
