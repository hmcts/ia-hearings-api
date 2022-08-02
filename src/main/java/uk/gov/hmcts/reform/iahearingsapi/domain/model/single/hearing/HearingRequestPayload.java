package uk.gov.hmcts.reform.iahearingsapi.domain.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.model.service.hearingvalues.PartyDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingRequestPayload {

    private RequestDetails requestDetails;

    private HearingDetails hearingDetails;

    private CaseDetails caseDetails;

    @JsonProperty("partyDetails")
    private List<PartyDetails> partiesDetails;
}
