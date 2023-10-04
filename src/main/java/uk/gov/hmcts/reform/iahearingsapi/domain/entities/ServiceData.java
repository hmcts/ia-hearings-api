package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Optional;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;

public class ServiceData extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServiceData() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    public <T> Optional<T> read(ServiceDataFieldDefinition extractor, Class<T> type) {
        return this.read(extractor);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> read(ServiceDataFieldDefinition extractor) {

        Object o = this.get(extractor.value());

        if (o == null) {
            return Optional.empty();
        }

        Object value = objectMapper.convertValue(o, extractor.getTypeReference());

        return Optional.of((T) value);
    }

    public <T> void write(ServiceDataFieldDefinition extractor, T value) {
        this.put(extractor.value(), value);
    }

    public void clear(ServiceDataFieldDefinition extractor) {
        this.put(extractor.value(), null);
    }
}
