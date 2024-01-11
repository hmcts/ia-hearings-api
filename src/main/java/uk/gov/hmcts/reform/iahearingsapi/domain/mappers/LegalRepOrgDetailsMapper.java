package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;

@Component
public class LegalRepOrgDetailsMapper {

    public PartyDetailsModel map(AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getLegalRepOrgPartyId(asylumCase))
            .partyType(PartyType.ORG.getPartyType())
            .partyRole("LGRP")
            .organisationDetails(
                OrganisationDetailsModel.builder()
                    .organisationType(PartyType.ORG.getPartyType())
                    .name(caseDataMapper.getLegalRepCompany(asylumCase))
                    .cftOrganisationID(caseDataMapper.getLegalRepOrganisationIdentifier(asylumCase))
                    .build())
            .build();
    }

    public PartyDetailsModel map(BailCase bailCase, BailCaseDataToServiceHearingValuesMapper caseDataMapper) {

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getLegalRepOrgPartyId(bailCase))
            .partyType(PartyType.ORG.getPartyType())
            .partyRole("LGRP")
            .organisationDetails(
                OrganisationDetailsModel.builder()
                    .organisationType(PartyType.ORG.getPartyType())
                    .name(caseDataMapper.getLegalRepCompanyName(bailCase))
                    .cftOrganisationID(caseDataMapper.getLegalRepOrganisationIdentifier(bailCase))
                    .build())
            .build();
    }
}
