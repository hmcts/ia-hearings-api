package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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

    @Test
    void should_map_correctly() {

        when(caseFlagsMapper.getVulnerableDetails(asylumCase)).thenReturn("vulnerability details");
        when(caseFlagsMapper.getVulnerableFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCustodyStatus(asylumCase)).thenReturn("In detention");

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .custodyStatus("In detention")
            .vulnerabilityDetails("vulnerability details")
            .vulnerableFlag(true)
            .build();
        PartyDetailsModel expected = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .build();

        assertEquals(expected, new AppellantDetailsMapper().map(asylumCase, caseFlagsMapper));
    }
}
