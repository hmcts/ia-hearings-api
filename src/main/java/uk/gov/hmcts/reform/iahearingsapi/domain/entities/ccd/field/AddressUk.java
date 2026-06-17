package uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class AddressUk {

    @JsonProperty("AddressLine1")
    private Optional<String> addressLine1 = Optional.empty();

    @JsonProperty("AddressLine2")
    private Optional<String> addressLine2 = Optional.empty();

    @JsonProperty("AddressLine3")
    private Optional<String> addressLine3 = Optional.empty();

    @JsonProperty("PostTown")
    private Optional<String> postTown = Optional.empty();

    @JsonProperty("County")
    private Optional<String> county = Optional.empty();

    @JsonProperty("PostCode")
    private Optional<String> postCode = Optional.empty();

    @JsonProperty("Country")
    private Optional<String> country = Optional.empty();

    private AddressUk() {
        // noop -- for deserializer
    }
}
