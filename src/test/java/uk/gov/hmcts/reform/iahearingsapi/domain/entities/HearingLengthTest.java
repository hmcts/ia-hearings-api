package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class HearingLengthTest {

    @Test
    void test_from_valid_value() {
        int validValue = 60;

        Optional<HearingLength> result = HearingLength.from(validValue);

        assertTrue(result.isPresent());
        assertEquals(HearingLength.LENGTH_1_HOUR, result.get());
    }

    @Test
    void test_from_invalid_value() {
        int invalidValue = 45;

        Optional<HearingLength> result = HearingLength.from(invalidValue);

        assertTrue(result.isEmpty());
    }

    @Test
    void test_to_string() {
        assertEquals("30", HearingLength.LENGTH_30_MINUTES.toString());
    }

    @Test
    void test_to_meaning_full_string() {
        assertEquals("1 hour", HearingLength.LENGTH_1_HOUR.convertToHourMinuteString());
        assertEquals("2 hours 30 minutes", HearingLength.LENGTH_2_HOURS_30_MINUTES.convertToHourMinuteString());
        assertEquals("4 hours 30 minutes", HearingLength.LENGTH_4_HOURS_30_MINUTES.convertToHourMinuteString());
    }
}
