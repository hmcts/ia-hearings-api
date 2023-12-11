package uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
@ToString
public class BailPartyFlagIdValue {

    @JsonProperty("id")
    private String partyId;
    private BailStrategicCaseFlag value;

    private BailPartyFlagIdValue() {
        // noop -- for deserializer
    }

    public BailPartyFlagIdValue(
        String partyId,
        BailStrategicCaseFlag value
    ) {
        requireNonNull(partyId);
        requireNonNull(value);

        this.partyId = partyId;
        this.value = value;
    }

    public String getPartyId() {
        requireNonNull(partyId);
        return partyId;
    }

    public BailStrategicCaseFlag getValue() {
        requireNonNull(value);
        return value;
    }
}
