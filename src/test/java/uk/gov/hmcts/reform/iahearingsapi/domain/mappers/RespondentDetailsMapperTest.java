package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class RespondentDetailsMapperTest {

    public static final String HEARING_CHANNEL = "hearingChannel";
    public static final String PARTY_NAME = "partyName";
    public static final String ORG = "ORG";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;

    @Test
    void should_map_correctly() {

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
}
