package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;

@ExtendWith(MockitoExtension.class)
class LegalRepOrgDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private BailCase bailCase;
    @Mock
    private BailCaseDataToServiceHearingValuesMapper bailCaseDataMapper;

    @Test
    void should_asylum_map_correctly() {

        when(caseDataMapper.getLegalRepOrgPartyId(asylumCase)).thenReturn("partyId");
        when(caseDataMapper.getLegalRepCompany(asylumCase)).thenReturn("legaRepPartyName");
        when(caseDataMapper.getLegalRepOrganisationIdentifier(asylumCase)).thenReturn("legalRepOrgId");
        PartyDetailsModel expected = PartyDetailsModel.builder()
            .partyID("partyId")
            .partyType("ORG")
            .partyRole("LGRP")
            .organisationDetails(
                OrganisationDetailsModel.builder()
                            .organisationType("ORG")
                            .name("legaRepPartyName")
                            .cftOrganisationID("legalRepOrgId")
                            .build())
            .build();

        assertEquals(expected, new LegalRepOrgDetailsMapper().map(asylumCase, caseDataMapper));
    }

    @Test
    void should_bail_map_correctly() {

        when(bailCaseDataMapper.getLegalRepOrgPartyId(bailCase)).thenReturn("partyId");
        when(bailCaseDataMapper.getLegalRepCompanyName(bailCase)).thenReturn("legaRepPartyName");
        when(bailCaseDataMapper.getLegalRepOrganisationIdentifier(bailCase)).thenReturn("legalRepOrgId");

        PartyDetailsModel expected = PartyDetailsModel.builder()
            .partyID("partyId")
            .partyType("ORG")
            .partyRole("LGRP")
            .organisationDetails(
                OrganisationDetailsModel.builder()
                    .organisationType("ORG")
                    .name("legaRepPartyName")
                    .cftOrganisationID("legalRepOrgId")
                    .build())
            .build();

        assertEquals(expected, new LegalRepOrgDetailsMapper().map(bailCase, bailCaseDataMapper));
    }
}
