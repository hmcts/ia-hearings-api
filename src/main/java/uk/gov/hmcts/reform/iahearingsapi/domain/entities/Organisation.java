package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
public class Organisation {

    @JsonProperty("OrganisationID")
    private String organisationID;

    @JsonProperty("OrganisationName")
    private String organisationName;
}
