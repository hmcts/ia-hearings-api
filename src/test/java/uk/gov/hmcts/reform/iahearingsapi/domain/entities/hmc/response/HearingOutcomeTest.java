package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class HearingOutcomeTest {

    private static final String JSON = "{"
        + "\"hearingType\":\"substantive\","
        + "\"hearingFinalFlag\":\"true\","
        + "\"hearingResult\":\"partHeard\","
        + "\"hearingResultReasonType\":\"reason\","
        + "\"hearingResultDate\":\"2023-01-20\""
        + "}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialises_hearing_type_and_result_from_labels() throws JsonProcessingException {
        HearingOutcome hearingOutcome = objectMapper.readValue(JSON, HearingOutcome.class);

        assertEquals(HearingType.SUBSTANTIVE, hearingOutcome.getHearingType());
        assertEquals(HearingResult.PART_HEARD, hearingOutcome.getHearingResult());
        assertEquals("true", hearingOutcome.getHearingFinalFlag());
        assertEquals("reason", hearingOutcome.getHearingResultReasonType());
        assertEquals("2023-01-20", hearingOutcome.getHearingResultDate());
    }

    @Test
    void serialises_hearing_type_and_result_as_labels() throws JsonProcessingException {
        HearingOutcome hearingOutcome = HearingOutcome.builder()
            .hearingType(HearingType.CMR)
            .hearingResult(HearingResult.WITHDRAWN)
            .build();

        String json = objectMapper.writeValueAsString(hearingOutcome);

        assertThat(json)
            .contains("\"hearingType\":\"cmr\"")
            .contains("\"hearingResult\":\"withdrawn\"");
    }
}
