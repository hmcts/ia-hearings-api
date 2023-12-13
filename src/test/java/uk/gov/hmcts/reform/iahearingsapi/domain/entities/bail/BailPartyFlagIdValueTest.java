package uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagValue;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BailPartyFlagIdValueTest {

    private final String id = "1";
    private final BailStrategicCaseFlag value = new BailStrategicCaseFlag(
        "partyName",
        "partyRole",
        List.of(new CaseFlagDetail("id", CaseFlagValue.builder().build())));

    private final BailPartyFlagIdValue witnessFlagsIdValue = new BailPartyFlagIdValue(id, value);

    @Test
    void should_hold_onto_values() {
        assertEquals(id, witnessFlagsIdValue.getPartyId());
        assertEquals(value, witnessFlagsIdValue.getValue());
    }

    @Test
    void should_throw() {

        assertThatThrownBy(() -> new BailPartyFlagIdValue(null, value))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new BailPartyFlagIdValue(id, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
