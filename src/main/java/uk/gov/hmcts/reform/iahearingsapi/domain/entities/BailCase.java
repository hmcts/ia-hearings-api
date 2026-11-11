package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.config.JsonMapperFactory;

import java.util.HashMap;
import java.util.Optional;

public class BailCase extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = JsonMapperFactory.CUSTOM_MAPPER;

    public BailCase() {
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
