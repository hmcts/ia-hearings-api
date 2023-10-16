package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartyDetailsModel {

    private String partyID;
    private String partyType;
    private String partyName;
    private String partyRole;
    private IndividualDetailsModel individualDetails;
    private OrganisationDetailsModel organisationDetails;
    private List<UnavailabilityDayOfWeekModel> unavailabilityDOW;
    private List<UnavailabilityRangeModel> unavailabilityRanges;
}
