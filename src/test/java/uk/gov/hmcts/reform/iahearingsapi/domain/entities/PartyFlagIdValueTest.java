package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class PartyFlagIdValueTest {

    private final String id = "1";
    private final StrategicCaseFlag value = new StrategicCaseFlag(
        "partyName",
        "partyRole",
        List.of(new CaseFlagDetail("id", CaseFlagValue.builder().build())));

    private final PartyFlagIdValue witnessFlagsIdValue = new PartyFlagIdValue(id, value);

    @Test
    void should_hold_onto_values() {
        assertEquals(id, witnessFlagsIdValue.getPartyId());
        assertEquals(value, witnessFlagsIdValue.getValue());
    }

    @Test
    void should_throw() {

        assertThatThrownBy(() -> new PartyFlagIdValue(null, value))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PartyFlagIdValue(id, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
