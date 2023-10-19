package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Caseflags {

    private List<PartyFlagsModel> flags;

    @JsonProperty("flagAmendURL")
    private String flagAmendUrl;
}
