package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class AsylumCaseFieldDefinitionTest {

    @Test
    void mapped_to_equivalent_field_name() {
        Stream.of(AsylumCaseFieldDefinition.values())
            // filter out below variable because of CCD defs constrains to edit existing fields
            .filter(val -> !Set.of(
                CASE_MANAGEMENT_LOCATION,
                HEARING_CHANNEL
            ).contains(val))
            .forEach(v -> assertThat(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()))
                .isEqualTo(v.value()));
    }

}
