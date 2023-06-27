package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HmcHearingRequestPayload {

    private HearingDetails hearingDetails;

    private CaseDetails caseDetails;

    @JsonProperty("partyDetails")
    private List<PartyDetails> partiesDetails;
}
