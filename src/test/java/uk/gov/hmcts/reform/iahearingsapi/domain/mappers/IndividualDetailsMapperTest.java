package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CustodyStatus.IN_CUSTODY;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;

@ExtendWith(MockitoExtension.class)
class IndividualDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;

    @Test
    void mapper_should_map_correctly() {

        when(caseFlagsMapper.getCustodyStatus(asylumCase))
            .thenReturn(IN_CUSTODY.getValue());
        when(caseFlagsMapper.getVulnerableFlag(asylumCase))
            .thenReturn(true);
        when(caseFlagsMapper.getVulnerableDetails(asylumCase))
            .thenReturn("vulnerability details");

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .vulnerableFlag(true)
            .custodyStatus(IN_CUSTODY.getValue())
            .vulnerabilityDetails("vulnerability details")
            .build();

        assertEquals(individualDetails, IndividualDetailsMapper.map(asylumCase, caseFlagsMapper));
    }
}
