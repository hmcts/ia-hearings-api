package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HearingTypeTest {

    @ParameterizedTest
    @CsvSource({
        "SUBSTANTIVE, substantive",
        "CMR, cmr"
    })
    void has_correct_label(HearingType hearingType, String label) {
        assertEquals(label, hearingType.getLabel());
    }

    @ParameterizedTest
    @CsvSource({
        "SUBSTANTIVE, BFA1-SUB",
        "CMR, BFA1-CMR"
    })
    void has_correct_hmc_value(HearingType hearingType, String hmcValue) {
        assertEquals(hmcValue, hearingType.getHmcValue());
    }

    @ParameterizedTest
    @CsvSource({
        "substantive, SUBSTANTIVE",
        "cmr, CMR"
    })
    void from_returns_hearing_type_for_label(String label, HearingType expected) {
        assertEquals(Optional.of(expected), HearingType.from(label));
    }

    @Test
    void from_returns_empty_for_unknown_label() {
        assertEquals(Optional.empty(), HearingType.from("SUBSTANTIVE"));
        assertEquals(Optional.empty(), HearingType.from(null));
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, HearingType.values().length);
    }
}
