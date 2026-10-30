package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HearingTypeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @ParameterizedTest
    @CsvSource({
        "SUBSTANTIVE, substantive",
        "CMR, cmr"
    })
    void serialises_to_and_from_label(HearingType hearingType, String label) throws JsonProcessingException {
        assertEquals("\"" + label + "\"", objectMapper.writeValueAsString(hearingType));
        assertEquals(hearingType, objectMapper.readValue("\"" + label + "\"", HearingType.class));
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, HearingType.values().length);
    }
}
