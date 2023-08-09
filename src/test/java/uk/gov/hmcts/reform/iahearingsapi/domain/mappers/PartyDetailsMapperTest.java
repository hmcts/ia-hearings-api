package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class PartyDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private LegalRepDetailsMapper legalRepDetailsMapper;
    @Mock
    private AppellantDetailsMapper appellantDetailsMapper;
    @Mock
    private RespondentDetailsMapper respondentDetailsMapper;
    @Mock
    private SponsorDetailsMapper sponsorDetailsMapper;
    @Mock
    private WitnessDetailsMapper witnessDetailsMapper;

    @Test
    void should_map_correctly() {

        when(appellantDetailsMapper.map(asylumCase, caseFlagsMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(legalRepDetailsMapper.map()).thenReturn(PartyDetailsModel.builder().build());
        when(sponsorDetailsMapper.map()).thenReturn(PartyDetailsModel.builder().build());
        when(respondentDetailsMapper.map()).thenReturn(PartyDetailsModel.builder().build());
        when(witnessDetailsMapper.map()).thenReturn(PartyDetailsModel.builder().build());

        List<PartyDetailsModel> expected = Arrays.asList(
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build()
        );
        PartyDetailsMapper mapper = new PartyDetailsMapper(
            appellantDetailsMapper,
            legalRepDetailsMapper,
            respondentDetailsMapper,
            sponsorDetailsMapper,
            witnessDetailsMapper
        );

        assertEquals(expected, mapper.map(asylumCase, caseFlagsMapper));
    }
}
