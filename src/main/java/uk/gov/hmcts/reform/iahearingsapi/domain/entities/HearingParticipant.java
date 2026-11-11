package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingParticipant {

    private String partyId;
    private String firstName;
    private String lastName;
    private String role;
    private String attendanceType;
    private String organisation;
}
