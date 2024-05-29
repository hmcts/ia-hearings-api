package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class HearingChannelTypeChangingRadioButtonTest {

    @Test
    void test_from_valid_value() {
        String validValue = "In Person";

        Optional<HearingChannelTypeChangingRadioButton> result = HearingChannelTypeChangingRadioButton.from(validValue);

        assertTrue(result.isPresent());
        assertEquals(HearingChannelTypeChangingRadioButton.INTER, result.get());
    }

    @Test
    void test_from_invalid_value() {
        String invalidValue = "Invalid Value";

        Optional<HearingChannelTypeChangingRadioButton> result =
            HearingChannelTypeChangingRadioButton.from(invalidValue);

        assertTrue(result.isEmpty());
    }

    @Test
    void test_to_string() {
        assertEquals("In Person", HearingChannelTypeChangingRadioButton.INTER.toString());
    }
}
