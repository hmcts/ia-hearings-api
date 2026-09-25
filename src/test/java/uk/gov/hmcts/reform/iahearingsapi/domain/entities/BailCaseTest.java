package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL;

@SuppressWarnings("OperatorWrap")
class BailCaseTest {

    private final ObjectMapper objectMapper = new JsonMapper();

    @Test
    void reads_string() throws IOException {

        String caseData = "{\"caseNameHmctsInternal\": \"caseNameHmctsInternal\"}";
        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);

        Optional<String> maybeAppealReferenceNumber = bailCase.read(CASE_NAME_HMCTS_INTERNAL);

        assertThat(maybeAppealReferenceNumber.get()).isEqualTo("caseNameHmctsInternal");
    }

    @Test
    void writes_value() {

        BailCase bailCase = new BailCase();

        bailCase.write(CASE_NAME_HMCTS_INTERNAL, "caseNameHmctsInternal");

        assertThat(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get())
            .isEqualTo("caseNameHmctsInternal");
    }

    @Test
    void clears_value() throws IOException {

        String caseData = "{\"caseNameHmctsInternal\": \"caseNameHmctsInternal\"}";
        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);

        bailCase.clear(CASE_NAME_HMCTS_INTERNAL);

        assertThat(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class)).isEmpty();
    }
}
