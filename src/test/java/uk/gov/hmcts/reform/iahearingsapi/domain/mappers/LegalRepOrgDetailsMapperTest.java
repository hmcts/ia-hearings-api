package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class LegalRepOrgDetailsMapperTest {

    private static final String COMPANY_NAME = "companyName";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;

    @Test
    void should_map_correctly() {

        when(asylumCase.read(LEGAL_REP_COMPANY_NAME, String.class)).thenReturn(Optional.of(COMPANY_NAME));
        when(caseDataMapper.getLegalRepOrgPartyId(asylumCase)).thenReturn("partyId");

        PartyDetailsModel expected = PartyDetailsModel.builder()
            .partyID("partyId")
            .partyType("ORG")
            .partyRole("LGRP")
            .organisationDetails(
                OrganisationDetailsModel.builder()
                    .name(COMPANY_NAME)
                    .organisationType("ORG")
                    .build())
            .build();

        assertEquals(expected, new LegalRepOrgDetailsMapper().map(asylumCase, caseDataMapper));
    }
}
