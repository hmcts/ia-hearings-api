package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class WitnessDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;

    @Test
    void should_map_correctly() {

        when(caseDataMapper.getPartyId()).thenReturn("partyId");
        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn("hearingChannel");

        final List<IdValue<WitnessDetails>> witnessDetails = List.of(
            new IdValue<>(
                "id1", new WitnessDetails("witnessName", "witnessFamilyName"))
        );
        when(asylumCase.read(WITNESS_DETAILS)).thenReturn(Optional.of(witnessDetails));

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName("witnessName")
            .lastName("witnessFamilyName")
            .preferredHearingChannel("hearingChannel")
            .build();
        List<PartyDetailsModel> expected = List.of(
            PartyDetailsModel.builder()
                .individualDetails(individualDetails)
                .partyID("partyId")
                .partyType("IND")
                .partyRole("WITN")
                .build()
        );

        assertEquals(expected, new WitnessDetailsMapper().map(asylumCase, caseDataMapper));
    }
}
