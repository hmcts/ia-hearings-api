package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;

@Component
public class RespondentDetailsMapper {

    private static final String BAIL_HOME_OFFICE_NAME = "Secretary of State";

    public PartyDetailsModel map(AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getRespondentPartyId(asylumCase))
            .partyType(PartyType.ORG.getPartyType())
            .partyRole("RESP")
            .organisationDetails(
                OrganisationDetailsModel.builder()
                    .organisationType(PartyType.ORG.getPartyType())
                    .name(caseDataMapper.getRespondentName(asylumCase))
                    .build())
            .build();
    }

    public PartyDetailsModel map(BailCase bailCase, BailCaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getRespondentPartyId(bailCase))
            .partyType(PartyType.ORG.getPartyType())
            .partyRole("RESP")
            .organisationDetails(
                OrganisationDetailsModel.builder()
                    .organisationType(PartyType.ORG.getPartyType())
                    .name(BAIL_HOME_OFFICE_NAME)
                    .build())
            .build();
    }
}
