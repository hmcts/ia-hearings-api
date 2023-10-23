package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EventTest {

    @Test
    void has_correct_values() {
        assertEquals("listCase", Event.LIST_CASE.toString());
        assertEquals("updateHearingRequest", Event.UPDATE_HEARING_REQUEST.toString());
        assertEquals("handleHearingException", Event.HANDLE_HEARING_EXCEPTION.toString());
        assertEquals("updateHMCResponse", Event.UPDATE_HMC_RESPONSE.toString());
        assertEquals("recordAdjournmentDetails", Event.RECORD_ADJOURNMENT_DETAILS.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
        assertEquals("endAppeal", Event.END_APPEAL.toString());
        assertEquals("editCaseListing", Event.EDIT_CASE_LISTING.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(8, Event.values().length);
    }
}
