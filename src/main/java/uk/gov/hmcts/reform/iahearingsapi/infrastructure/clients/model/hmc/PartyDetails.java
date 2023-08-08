package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityDayOfWeekModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.UnavailabilityRangeModel;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyDetails {

    private String partyID;

    private String partyType;

    private String partyRole;

    private IndividualDetailsModel individualDetails;

    private String partyChannelSubType;

    private OrganisationDetailsModel organisationDetails;

    @JsonProperty("unavailabilityDOW")
    private List<UnavailabilityDayOfWeekModel> unavailabilityDayOfWeek;

    private List<UnavailabilityRangeModel> unavailabilityRanges;
}
