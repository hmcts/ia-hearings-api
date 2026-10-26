package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualDayParty {

    private String actualPartyId;

    private String partyRole;

    private ActualIndividualDetails individualDetails;

    private String partyChannelSubType;

    private String didNotAttendFlag;

    private String representedParty;
}
