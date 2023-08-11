package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@Component
public class AppellantDetailsMapper {

    public PartyDetailsModel map(
        AsylumCase asylumCase, CaseFlagsToServiceHearingValuesMapper caseFlagsMapper) {

        return PartyDetailsModel.builder()
            .individualDetails(IndividualDetailsMapper.map(asylumCase, caseFlagsMapper))
            .build();
    }
}
