package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class RespondentDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;

    @Test
    void should_map_correctly() {

        when(caseDataMapper.getPartyId()).thenReturn("partyId");

        PartyDetailsModel expected = PartyDetailsModel.builder()
            .partyID("partyId")
            .partyType("ORG")
            .partyRole("RESP")
            .organisationDetails(
                List.of(OrganisationDetailsModel.builder()
                            .organisationType("ORG")
                            .build()))
            .build();

        assertEquals(expected, new RespondentDetailsMapper().map(asylumCase, caseDataMapper));
    }
}
