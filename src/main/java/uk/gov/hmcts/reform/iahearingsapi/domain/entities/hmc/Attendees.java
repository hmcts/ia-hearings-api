package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendees {

    private String partyID;

    private HearingSubChannel hearingSubChannel;
}
