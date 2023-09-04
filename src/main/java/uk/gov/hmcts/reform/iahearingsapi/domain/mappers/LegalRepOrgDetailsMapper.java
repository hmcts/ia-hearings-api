package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_NAME;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;

@Component
public class LegalRepOrgDetailsMapper {

    public PartyDetailsModel map(AsylumCase asylumCase, CaseDataToServiceHearingValuesMapper caseDataMapper) {

        String legalRepCompanyName = asylumCase.read(LEGAL_REP_COMPANY_NAME, String.class).orElse(null);

        return PartyDetailsModel.builder()
            .partyID(caseDataMapper.getLegalRepOrgPartyId(asylumCase))
            .partyType(PartyType.ORG.getPartyType())
            .partyRole("LGRP")
            .organisationDetails(
                List.of(OrganisationDetailsModel.builder()
                            .organisationType(PartyType.ORG.getPartyType())
                            .name(legalRepCompanyName)
                            .build()))
            .build();
    }
}
