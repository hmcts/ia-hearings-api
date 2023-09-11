package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.IS_SINGLE_SEX_COURT_ALLOWED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SINGLE_SEX_COURT_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.JourneyType.REP;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.GrantedRefusedType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.SingleSexType;
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

    @BeforeEach
    void setup() {
        when(caseDataMapper.getAppellantPartyId(asylumCase)).thenReturn("partyId");
        when(asylumCase.read(JOURNEY_TYPE, String.class)).thenReturn(Optional.of(REP.getValue()));
    }

    @Test
    void should_map_correctly() {

        when(caseFlagsMapper.getVulnerableDetails(asylumCase)).thenReturn("vulnerability details");
        when(caseFlagsMapper.getVulnerableFlag(asylumCase)).thenReturn(true);
        when(caseFlagsMapper.getCustodyStatus(asylumCase)).thenReturn("In detention");
        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn("hearingChannel");

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .custodyStatus("In detention")
            .vulnerabilityDetails("vulnerability details")
            .vulnerableFlag(true)
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .preferredHearingChannel("hearingChannel")
            .otherReasonableAdjustmentDetails("")
            .build();
        PartyDetailsModel expected = getPartyDetailsModelForAppellant(individualDetails);

        assertEquals(expected, new AppellantDetailsMapper().map(asylumCase, caseFlagsMapper, caseDataMapper));
    }

    @Test
    void should_return_female_statement_when_granted_for_female() {
        when(asylumCase.read(IS_SINGLE_SEX_COURT_ALLOWED, String.class))
            .thenReturn(Optional.of(GrantedRefusedType.GRANTED.getValue()));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, String.class))
            .thenReturn(Optional.of(SingleSexType.FEMALE.getValue()));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .vulnerableFlag(false)
            .otherReasonableAdjustmentDetails("Single sex court: Female;")
            .build();
        PartyDetailsModel expected = getPartyDetailsModelForAppellant(individualDetails);

        assertEquals(expected, new AppellantDetailsMapper().map(asylumCase, caseFlagsMapper, caseDataMapper));
    }

    @Test
    void should_return_male_statement_when_granted_for_male() {
        when(asylumCase.read(IS_SINGLE_SEX_COURT_ALLOWED, String.class))
            .thenReturn(Optional.of(GrantedRefusedType.GRANTED.getValue()));
        when(asylumCase.read(SINGLE_SEX_COURT_TYPE, String.class))
            .thenReturn(Optional.of(SingleSexType.MALE.getValue()));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .vulnerableFlag(false)
            .otherReasonableAdjustmentDetails("Single sex court: Male;")
            .build();
        PartyDetailsModel expected = getPartyDetailsModelForAppellant(individualDetails);

        assertEquals(expected, new AppellantDetailsMapper().map(asylumCase, caseFlagsMapper, caseDataMapper));
    }

    private PartyDetailsModel getPartyDetailsModelForAppellant(IndividualDetailsModel individualDetails) {
        return PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("APEL")
            .unavailabilityRanges(Collections.emptyList())
            .unavailabilityDOW(Collections.emptyList())
            .build();
    }
}
