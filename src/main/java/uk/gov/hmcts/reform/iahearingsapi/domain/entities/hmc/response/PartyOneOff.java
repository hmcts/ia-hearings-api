package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PartyOneOff {

    private IndividualDetails individualDetails;

    private OrganisationDetails organisationDetails;
}
