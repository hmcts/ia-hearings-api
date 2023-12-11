package uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.LanguageAndAdjustmentsMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicantDetailsMapperTest {

    @Mock
    private BailCase bailCase;
    @Mock
    private BailCaseFlagsToServiceHearingValuesMapper caseFlagsMapper;
    @Mock
    private BailCaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private LanguageAndAdjustmentsMapper languageAndAdjustmentsMapper;

    @BeforeEach
    void setup() {
        when(caseDataMapper.getApplicantPartyId(bailCase)).thenReturn("partyId");
    }

    @Test
    void should_map_correctly() {

        when(caseFlagsMapper.getVulnerableDetails(bailCase)).thenReturn("vulnerability details");
        when(caseFlagsMapper.getVulnerableFlag(bailCase)).thenReturn(true);
        when(caseDataMapper.getHearingChannel(bailCase)).thenReturn("VID");

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .custodyStatus("D")
            .vulnerabilityDetails("vulnerability details")
            .vulnerableFlag(true)
            .preferredHearingChannel("VID")
            .build();
        PartyDetailsModel expected = getPartyDetailsModelForApplicant(individualDetails);
        when(languageAndAdjustmentsMapper.processBailPartyCaseFlags(eq(bailCase), any(PartyDetailsModel.class)))
            .thenReturn(expected);

        expected.getIndividualDetails().setOtherReasonableAdjustmentDetails(null);

        assertEquals(expected, new ApplicantDetailsMapper(languageAndAdjustmentsMapper)
            .map(bailCase, caseFlagsMapper, caseDataMapper));
    }

    private PartyDetailsModel getPartyDetailsModelForApplicant(IndividualDetailsModel individualDetails) {
        return PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("APPL")
            .build();
    }
}
