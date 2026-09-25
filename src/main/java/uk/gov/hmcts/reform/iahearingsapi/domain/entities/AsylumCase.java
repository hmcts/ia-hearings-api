package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import java.util.HashMap;
import java.util.Optional;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseData;

public class AsylumCase extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = new JsonMapper();

    public AsylumCase() {
    }

    public static AsylumCase copy(AsylumCase original) {
        AsylumCase copy =  new AsylumCase();
        copy.putAll(original);
        return copy;
    }

    public <T> Optional<T> read(AsylumCaseFieldDefinition extractor, Class<T> type) {
        return this.read(extractor);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> read(AsylumCaseFieldDefinition extractor) {

        Object o = this.get(extractor.value());

        if (o == null) {
            return Optional.empty();
        }

        Object value = objectMapper.convertValue(o, extractor.getTypeReference());

        return Optional.of((T) value);
    }

    public <T> void write(AsylumCaseFieldDefinition extractor, T value) {
        this.put(extractor.value(), value);
    }

    public void clear(AsylumCaseFieldDefinition extractor) {
        this.put(extractor.value(), null);
    }
}
