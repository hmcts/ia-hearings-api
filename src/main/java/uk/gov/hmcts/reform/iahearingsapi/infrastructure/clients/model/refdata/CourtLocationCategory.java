package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Jacksonized
@AllArgsConstructor
public class CourtLocationCategory {

    private String serviceCode;
    private String courtTypeId;
    private String courtType;
    private List<CourtVenue> courtVenues;
}
