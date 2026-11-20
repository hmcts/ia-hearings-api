package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Party {

    @JsonAlias("partyID")
    private String partyId;

    private String partyRole;

    private String partyType;

    private List<PartyOneOff> oneOf;

    private String partyChannelSubType;

    @JsonSetter("individualDetails")
    void addIndividualDetails(IndividualDetails individualDetails) {
        if (individualDetails != null) {
            addOneOf(PartyOneOff.builder().individualDetails(individualDetails).build());
        }
    }

    @JsonSetter("organisationDetails")
    void addOrganisationDetails(OrganisationDetails organisationDetails) {
        if (organisationDetails != null) {
            addOneOf(PartyOneOff.builder().organisationDetails(organisationDetails).build());
        }
    }

    private void addOneOf(PartyOneOff details) {
        List<PartyOneOff> all = oneOf == null ? new ArrayList<>() : new ArrayList<>(oneOf);
        all.add(details);
        oneOf = all;
    }
}
