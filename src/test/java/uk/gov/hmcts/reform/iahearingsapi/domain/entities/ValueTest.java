package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ValueTest {

    private final String code = "code";
    private final String label = "label";
    private final Value value = new Value(code, label);

    @Test
    void should_hold_onto_values() {

        assertEquals(value.getLabel(), label);
        assertEquals(value.getCode(), code);
    }

}
