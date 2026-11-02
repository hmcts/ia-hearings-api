package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HearingResultTest {

    @ParameterizedTest
    @CsvSource({
        "HEARD, heard",
        "PART_HEARD, partHeard",
        "WITHDRAWN, withdrawn"
    })
    void has_correct_label(HearingResult hearingResult, String label) {
        assertEquals(label, hearingResult.getLabel());
    }

    @ParameterizedTest
    @CsvSource({
        "HEARD, COMPLETED",
        "PART_HEARD, ADJOURNED",
        "WITHDRAWN, CANCELLED"
    })
    void has_correct_hmc_value(HearingResult hearingResult, String hmcValue) {
        assertEquals(hmcValue, hearingResult.getHmcValue());
    }

    @ParameterizedTest
    @CsvSource({
        "heard, HEARD",
        "partHeard, PART_HEARD",
        "withdrawn, WITHDRAWN"
    })
    void from_returns_hearing_result_for_label(String label, HearingResult expected) {
        assertEquals(Optional.of(expected), HearingResult.from(label));
    }

    @Test
    void from_returns_empty_for_unknown_label() {
        assertEquals(Optional.empty(), HearingResult.from("HEARD"));
        assertEquals(Optional.empty(), HearingResult.from("COMPLETED"));
        assertEquals(Optional.empty(), HearingResult.from(null));
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, HearingResult.values().length);
    }
}
