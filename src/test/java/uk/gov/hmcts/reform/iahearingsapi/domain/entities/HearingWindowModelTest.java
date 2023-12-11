package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;

public class HearingWindowModelTest {

    private static final String DATE_START = "dateStart";

    @Test
    void should_not_default_to_null_if_any_field_is_not_null() {
        HearingWindowModel hearingWindowModel = HearingWindowModel
            .builder()
            .dateRangeStart(DATE_START)
            .build();

        assertNotNull(hearingWindowModel.getHearingWindowModel());
    }

    @Test
    void should_default_to_null_if_all_fields_are_not_null() {
        HearingWindowModel hearingWindowModel = HearingWindowModel
            .builder()
            .build();

        assertNull(hearingWindowModel.getHearingWindowModel());
    }
}
