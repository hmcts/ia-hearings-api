package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class PartyDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private LegalRepDetailsMapper legalRepDetailsMapper;
    @Mock
    private LegalRepOrgDetailsMapper legalRepOrgDetailsMapper;
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

        when(appellantDetailsMapper.map(asylumCase, caseFlagsMapper, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(legalRepDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(legalRepOrgDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(sponsorDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(respondentDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(PartyDetailsModel.builder().build());
        when(witnessDetailsMapper.map(asylumCase, caseDataMapper))
            .thenReturn(List.of(PartyDetailsModel.builder().build()));
        when(asylumCase.read(AsylumCaseFieldDefinition.HAS_SPONSOR, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        List<PartyDetailsModel> expected = Arrays.asList(
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build(),
            PartyDetailsModel.builder().build()
        );
        PartyDetailsMapper mapper = new PartyDetailsMapper(
            appellantDetailsMapper,
            legalRepDetailsMapper,
            legalRepOrgDetailsMapper,
            respondentDetailsMapper,
            sponsorDetailsMapper,
            witnessDetailsMapper
        );

        assertEquals(expected, mapper.map(asylumCase, caseFlagsMapper, caseDataMapper));
    }
}
