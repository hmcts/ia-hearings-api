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
    // sub-channel picked from the FixedList, used on attendees added on the page
    private String attendanceType;
    // sub-channel picked from HearingSubChannel ref data, used on pre-filled attendees (list built by ia-case-api)
    private DynamicList attendanceTypeList;
    private String organisation;
    private YesOrNo prefilled;
}
