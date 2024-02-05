package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_MOBILE_NUMBER;

public class BailCaseFieldDefinitionTest {

    private List<BailCaseFieldDefinition> exceptionalFields = List.of(APPLICANT_MOBILE_NUMBER);
    @Test
    void mapped_to_equivalent_field_name() {
        Stream.of(BailCaseFieldDefinition.values())
            .filter(v -> !exceptionalFields.contains(v))
            .forEach(field -> assertThat(UPPER_UNDERSCORE.to(LOWER_CAMEL, field.name()))
                .isEqualTo(field.value()));
    }
}
