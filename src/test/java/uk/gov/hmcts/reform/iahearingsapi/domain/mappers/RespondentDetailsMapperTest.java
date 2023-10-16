package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class RespondentDetailsMapperTest {

    public static final String HEARING_CHANNEL = "hearingChannel";

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;

    @Test
    void should_map_correctly() {

        when(caseDataMapper.getRespondentPartyId(asylumCase)).thenReturn("partyId");
        when(caseDataMapper.getRespondentName(asylumCase)).thenReturn("partyName");
        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn(HEARING_CHANNEL);

        PartyDetailsModel expected = PartyDetailsModel.builder()
            .partyID("partyId")
            .partyType("IND")
            .partyRole("RESP")
            .individualDetails(
                IndividualDetailsModel.builder()
                    .firstName("partyName")
                    .lastName("(Home Office)")
                    .preferredHearingChannel(HEARING_CHANNEL)
                    .build())
            .build();

        assertEquals(expected, new RespondentDetailsMapper().map(asylumCase, caseDataMapper));
    }
}
