package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class HearingGetResponse {

    private HearingRequestDetails requestDetails;

    private HearingDetails hearingDetails;

    private CaseDetailsHearing caseDetails;

    private List<PartyDetailsModel> partyDetails;

    private HearingResponse hearingResponse;

}
