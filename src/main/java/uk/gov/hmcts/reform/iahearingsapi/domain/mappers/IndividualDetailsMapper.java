package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;

public class IndividualDetailsMapper {

    private IndividualDetailsMapper() {
    }

    public static IndividualDetailsModel map(
        AsylumCase asylumCase, CaseFlagsToServiceHearingValuesMapper caseFlagsMapper) {
        return IndividualDetailsModel.builder()
            .vulnerableFlag(caseFlagsMapper.getVulnerableFlag(asylumCase))
            .vulnerabilityDetails(caseFlagsMapper.getVulnerableDetails(asylumCase))
            .custodyStatus(caseFlagsMapper.getCustodyStatus(asylumCase))
            .build();
    }
}
