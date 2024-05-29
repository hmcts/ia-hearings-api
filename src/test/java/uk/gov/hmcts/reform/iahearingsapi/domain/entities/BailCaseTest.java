package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.CASE_NAME_HMCTS_INTERNAL;

@SuppressWarnings("OperatorWrap")
class BailCaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void reads_string() throws IOException {

        String caseData = "{\"caseNameHmctsInternal\": \"caseNameHmctsInternal\"}";
        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);

        Optional<String> maybeAppealReferenceNumber = bailCase.read(CASE_NAME_HMCTS_INTERNAL);

        Assertions.assertEquals(maybeAppealReferenceNumber.get(), "caseNameHmctsInternal");
    }

    @Test
    void writes_value() {

        BailCase bailCase = new BailCase();

        bailCase.write(CASE_NAME_HMCTS_INTERNAL, "caseNameHmctsInternal");

        Assertions.assertEquals(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class).get(),
            ("caseNameHmctsInternal"));
    }

    @Test
    void clears_value() throws IOException {

        String caseData = "{\"caseNameHmctsInternal\": \"caseNameHmctsInternal\"}";
        BailCase bailCase = objectMapper.readValue(caseData, BailCase.class);

        bailCase.clear(CASE_NAME_HMCTS_INTERNAL);

        assertThat(bailCase.read(CASE_NAME_HMCTS_INTERNAL, String.class)).isEmpty();
    }
}
