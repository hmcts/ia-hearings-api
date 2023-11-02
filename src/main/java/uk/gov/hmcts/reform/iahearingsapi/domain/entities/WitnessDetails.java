package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WitnessDetails {

    private String witnessPartyId;
    private String witnessName;
    private String witnessFamilyName;
    private YesOrNo isWitnessDeleted;

    public String buildWitnessFullName() {
        String givenNames = getWitnessName() == null ? " " : getWitnessName();
        String familyName = getWitnessFamilyName() == null ? " " : getWitnessFamilyName();

        return !(givenNames.isBlank() || familyName.isBlank()) ? givenNames + " " + familyName : givenNames;
    }
}
