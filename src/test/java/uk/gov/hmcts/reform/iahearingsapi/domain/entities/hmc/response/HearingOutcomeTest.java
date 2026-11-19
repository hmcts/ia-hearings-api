package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class HearingOutcomeTest {

    private static final String JSON = "{"
        + "\"hearingType\":\"BFA1-SUB\","
        + "\"hearingFinalFlag\":true,"
        + "\"hearingResult\":\"ADJOURNED\","
        + "\"hearingResultReasonType\":\"reason\","
        + "\"hearingResultDate\":\"2023-01-20\""
        + "}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialises_hearing_outcome() throws JsonProcessingException {
        HearingOutcome hearingOutcome = objectMapper.readValue(JSON, HearingOutcome.class);

        assertEquals("BFA1-SUB", hearingOutcome.getHearingType());
        assertEquals("ADJOURNED", hearingOutcome.getHearingResult());
        assertTrue(hearingOutcome.isHearingFinalFlag());
        assertEquals("reason", hearingOutcome.getHearingResultReasonType());
        assertEquals("2023-01-20", hearingOutcome.getHearingResultDate());
    }

    @Test
    void serialises_hearing_outcome() throws JsonProcessingException {
        HearingOutcome hearingOutcome = HearingOutcome.builder()
            .hearingType(HearingType.CMR.getHmcValue())
            .hearingResult(HearingResult.CANCELLED.getHmcValue())
            .build();

        String json = objectMapper.writeValueAsString(hearingOutcome);

        assertThat(json)
            .contains("\"hearingType\":\"BFA1-CMR\"")
            .contains("\"hearingResult\":\"CANCELLED\"");
    }
}
