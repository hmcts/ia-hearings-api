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
        assertEquals("unknown", Event.UNKNOWN.toString());
        assertEquals("endAppeal", Event.END_APPEAL.toString());
        assertEquals("startAppeal", Event.START_APPEAL.toString());
        assertEquals("editAppeal", Event.EDIT_APPEAL.toString());
        assertEquals("submitAppeal", Event.SUBMIT_APPEAL.toString());
        assertEquals("listCaseForFTOnly", Event.LIST_CASE_FOR_FT_ONLY.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(10, Event.values().length);

    }
}
