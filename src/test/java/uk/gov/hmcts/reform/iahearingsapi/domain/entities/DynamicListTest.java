package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DynamicListTest {

    private final String valueStr = "value";
    private final Value value = new Value(valueStr, valueStr);
    private final List<Value> listItems = List.of(value);

    @Test
    void should_hold_onto_values() {
        DynamicList dynamicList = new DynamicList(valueStr);
        assertThat(dynamicList.getValue()).usingRecursiveComparison().isEqualTo(value);

        dynamicList = new DynamicList(value, listItems);
        assertThat(dynamicList.getValue()).usingRecursiveComparison().isEqualTo(value);
        assertThat(dynamicList.getListItems()).usingRecursiveComparison().isEqualTo(listItems);

        dynamicList.setValue(value);
        assertThat(dynamicList.getValue()).usingRecursiveComparison().isEqualTo(value);
    }
}
