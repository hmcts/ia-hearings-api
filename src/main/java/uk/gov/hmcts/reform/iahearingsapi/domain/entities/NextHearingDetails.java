package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NextHearingDetails {

    @JsonProperty("hearingID")
    private String hearingId;
    private String hearingDateTime;
}
