package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class AppellantDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;

    @Test
    void should_map_correctly() {

        when(caseFlagsMapper.getVulnerableDetails(asylumCase)).thenReturn("vulnerability details");
        when(caseFlagsMapper.getVulnerableFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCustodyStatus(asylumCase)).thenReturn("In detention");
        when(caseDataMapper.getPartyId()).thenReturn("partyId");
        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn("hearingChannel");

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .custodyStatus("In detention")
            .vulnerabilityDetails("vulnerability details")
            .vulnerableFlag(true)
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .preferredHearingChannel("hearingChannel")
            .build();
        PartyDetailsModel expected = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("APEL")
            .unavailabilityRanges(Collections.emptyList())
            .unavailabilityDOW(Collections.emptyList())
            .build();

        assertEquals(expected, new AppellantDetailsMapper().map(asylumCase, caseFlagsMapper, caseDataMapper));
    }
}
