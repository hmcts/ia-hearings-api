package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;

@Component
public class RespondentDetailsMapper {

    public PartyDetailsModel map(AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getRespondentPartyId(asylumCase))
            .partyType(PartyType.IND.getPartyType())
            .partyRole("RESP")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName(caseDataMapper.getRespondentName(asylumCase))
                    .lastName("(Home Office)")
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase))
                    .build())
            .build();
    }
}
