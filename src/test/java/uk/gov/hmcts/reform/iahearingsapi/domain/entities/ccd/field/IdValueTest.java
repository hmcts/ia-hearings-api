package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IdValueTest {

    private final String id = "1";
    private final Integer value = 1234;

    private final IdValue<Integer> addressUk = new IdValue<>(id, value);

    @Test
    void should_hold_onto_values() {
        assertEquals(id, addressUk.getId());
        assertEquals(value, addressUk.getValue());
    }

    @Test
    void should_throw() {

        assertThatThrownBy(() -> new IdValue<>(null, value))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new IdValue<>(id, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
