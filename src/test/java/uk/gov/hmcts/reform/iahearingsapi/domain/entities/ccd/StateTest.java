package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StateTest {

    @Test
    void has_correct_values() {
        assertEquals("prepareForHearing", State.PREPARE_FOR_HEARING.toString());
        assertEquals("updateHearingRequest", State.UPDATE_HEARING_REQUEST.toString());
        assertEquals("unknown", State.UNKNOWN.toString());
    }

    @Test
    void fail_if_changes_needed_after_modifying_class() {
        assertEquals(3, Event.values().length);
    }
}
