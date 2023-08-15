package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartyFlagsModel {

    @JsonProperty("partyID")
    private String partyId;
    private String partyName;
    private String flagParentId;
    private String flagId;
    private String flagDescription;
    private String flagStatus;
}
