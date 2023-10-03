package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;

@Component
public class RespondentDetailsMapper {

    public PartyDetailsModel map(AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getRespondentPartyId(asylumCase))
            .partyType(PartyType.IND.getPartyType())
            .partyRole("RESP")
            // For displaying Respondent as an individual in the Hearings tab Participant attendance page.
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName("Home")
                    .lastName("Office")
                    .preferredHearingChannel(caseDataMapper.getHearingChannel(asylumCase))
                    .build())
            .organisationDetails(
                OrganisationDetailsModel.builder()
                        .organisationType(PartyType.ORG.getPartyType())
                        .name(caseDataMapper.getRespondentName(asylumCase))
                        .cftOrganisationID(caseDataMapper.getRespondentPartyId(asylumCase))
                        .build())
            .build();
    }
}
