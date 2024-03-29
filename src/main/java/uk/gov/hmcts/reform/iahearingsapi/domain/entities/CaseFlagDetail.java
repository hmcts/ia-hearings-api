package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlagDetail {

    String id;
    @JsonProperty("value")
    CaseFlagValue caseFlagValue;

}
