package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.assertj.core.api.Assertions.assertThat;

public class BailCaseFieldDefinitionTest {

    @Test
    void mapped_to_equivalent_field_name() {
        Stream.of(BailCaseFieldDefinition.values())
            .forEach(v -> assertThat(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()))
                .isEqualTo(v.value()));
    }
}
