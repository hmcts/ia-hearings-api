package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HMCTS_CASE_NAME_INTERNAL;

import java.io.IOException;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OperatorWrap")
class AsylumCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void reads_string() throws IOException {

        String caseData = "{\"hmctsCaseNameInternal\": \"hmctsCaseNameInternal\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        Optional<String> maybeAppealReferenceNumber = asylumCase.read(HMCTS_CASE_NAME_INTERNAL);

        assertThat(maybeAppealReferenceNumber.get()).isEqualTo("hmctsCaseNameInternal");
    }

    @Test
    void writes_value() {

        AsylumCase asylumCase = new AsylumCase();

        asylumCase.write(HMCTS_CASE_NAME_INTERNAL, "hmctsCaseNameInternal");

        assertThat(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class).get())
            .isEqualTo("hmctsCaseNameInternal");
    }

    @Test
    void clears_value() throws IOException {

        String caseData = "{\"hmctsCaseNameInternal\": \"hmctsCaseNameInternal\"}";
        AsylumCase asylumCase = objectMapper.readValue(caseData, AsylumCase.class);

        asylumCase.clear(HMCTS_CASE_NAME_INTERNAL);

        assertThat(asylumCase.read(HMCTS_CASE_NAME_INTERNAL, String.class)).isEmpty();
    }
}
