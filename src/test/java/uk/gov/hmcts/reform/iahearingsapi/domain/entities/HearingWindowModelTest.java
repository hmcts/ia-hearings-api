package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;

public class HearingWindowModelTest {

    private static final String DATE_START = "dateStart";

    @Test
    void should_not_return_true_if_any_field_is_not_null() {
        HearingWindowModel hearingWindowModel = HearingWindowModel
            .builder()
            .dateRangeStart(DATE_START)
            .build();

        assertFalse(hearingWindowModel.allNull());
    }

    @Test
    void should_return_true_if_all_fields_are_null() {
        HearingWindowModel hearingWindowModel = HearingWindowModel
            .builder()
            .build();

        assertTrue(hearingWindowModel.allNull());
    }
}
