package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DocumentTagTest {

    @Test
    void has_correct_values() {

        assertEquals("bailSubmission", DocumentTag.BAIL_SUBMISSION.toString());
        assertEquals("", DocumentTag.NONE.toString());

    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, DocumentTag.values().length);
    }

}
