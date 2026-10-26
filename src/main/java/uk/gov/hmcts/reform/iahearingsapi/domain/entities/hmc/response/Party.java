package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Party {

    private String partyId;

    private String partyRole;

    private String partyType;

    private List<PartyOneOff> oneOf;

    private String partyChannelSubType;
}
