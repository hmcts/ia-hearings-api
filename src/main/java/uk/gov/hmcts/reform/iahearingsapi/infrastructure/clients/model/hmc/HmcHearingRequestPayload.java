package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HmcHearingRequestPayload {

    private HearingDetails hearingDetails;

    private CaseDetails caseDetails;

    private List<PartyDetails> partyDetails;
}
