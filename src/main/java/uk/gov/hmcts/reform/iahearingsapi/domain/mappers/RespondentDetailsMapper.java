package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;

@Component
public class RespondentDetailsMapper {

    public PartyDetailsModel map(AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getPartyId())
            .partyType(PartyType.ORG.getPartyType())
            .partyRole("RESP")
            .organisationDetails(
                List.of(OrganisationDetailsModel.builder()
                        .organisationType(PartyType.ORG.getPartyType())
                        .name(caseDataMapper.getRespondentName(asylumCase))
                        .build()))
            .build();
    }
}
