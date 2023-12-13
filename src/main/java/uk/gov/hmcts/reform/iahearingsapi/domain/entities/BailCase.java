package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;

import java.util.HashMap;
import java.util.Optional;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;

public class BailCase extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BailCase() {
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.enable(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
    }

    public <T> Optional<T> read(BailCaseFieldDefinition extractor, Class<T> type) {
        return this.read(extractor);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> read(BailCaseFieldDefinition extractor) {

        Object o = this.get(extractor.value());

        if (o == null) {
            return Optional.empty();
        }

        Object value = objectMapper.convertValue(o, extractor.getTypeReference());

        return Optional.of((T) value);
    }

    public <T> void write(BailCaseFieldDefinition extractor, T value) {
        this.put(extractor.value(), value);
    }

    public void clear(BailCaseFieldDefinition extractor) {
        this.put(extractor.value(), null);
    }
}
