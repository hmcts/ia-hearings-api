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
    // read-only copies of the names, only set on attendees pre-populated from HMC
    private String firstNameLabel;
    private String lastNameLabel;
    private String roleLabel;
    private String role;
    private String representedParty;
    // sub-channel picked from HearingSubChannel ref data (list items added by ia-case-api)
    private DynamicList attendanceType;
    private String organisation;
    private YesOrNo prefilled;
}
