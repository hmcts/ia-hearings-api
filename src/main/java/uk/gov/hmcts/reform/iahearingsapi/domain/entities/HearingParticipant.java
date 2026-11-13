package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingParticipant {

    private String partyId;
    private String firstName;
    private String lastName;
    private String roleLabel;
    private String role;
    private DynamicList representedParty;
    private String attendanceType;
    private String organisation;
    private YesOrNo prefilled;
}
