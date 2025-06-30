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
class RespondentDetailsMapperTest {

    public static final String PARTY_NAME = "partyName";
    public static final String ORG = "ORG";

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

        when(caseDataMapper.getRespondentPartyId(asylumCase)).thenReturn("partyId");
        when(caseDataMapper.getRespondentName(asylumCase)).thenReturn(PARTY_NAME);

        PartyDetailsModel expected = PartyDetailsModel.builder()
            .partyID("partyId")
            .partyType(ORG)
            .partyRole("RESP")
            .organisationDetails(
                OrganisationDetailsModel.builder()
                    .organisationType(ORG)
                    .name(PARTY_NAME)
                    .cftOrganisationID(null)
                    .build())
            .build();

        assertEquals(expected, new RespondentDetailsMapper().map(asylumCase, caseDataMapper));
    }

    @Test
    void should_bail_map_correctly() {

        when(bailCaseDataMapper.getRespondentPartyId(bailCase)).thenReturn("partyId");

        PartyDetailsModel expected = PartyDetailsModel.builder()
            .partyID("partyId")
            .partyType(ORG)
            .partyRole("RESP")
            .organisationDetails(
                OrganisationDetailsModel.builder()
                    .organisationType(ORG)
                    .name("Secretary of State")
                    .cftOrganisationID(null)
                    .build())
            .build();

        assertEquals(expected, new RespondentDetailsMapper().map(bailCase, bailCaseDataMapper));
    }
}
