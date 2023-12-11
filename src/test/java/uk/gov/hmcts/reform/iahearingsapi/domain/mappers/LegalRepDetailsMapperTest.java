package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;

@ExtendWith(MockitoExtension.class)
class LegalRepDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private BailCase bailCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private BailCaseDataToServiceHearingValuesMapper bailCaseDataMapper;

    @Test
    void should_map_asylum_correctly() {

        when(caseDataMapper.getLegalRepPartyId(asylumCase)).thenReturn("partyId");
        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn("hearingChannel");

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .preferredHearingChannel("hearingChannel")
            .build();
        PartyDetailsModel expected = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("LGRP")
            .build();

        assertEquals(expected, new LegalRepDetailsMapper().map(asylumCase, caseDataMapper));
    }

    @Test
    void should_map_bail_correctly() {

        when(bailCaseDataMapper.getLegalRepPartyId(bailCase)).thenReturn("partyId");
        when(bailCaseDataMapper.getHearingChannel(bailCase)).thenReturn("VID");

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .preferredHearingChannel("VID")
            .build();
        PartyDetailsModel expected = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("LGRP")
            .build();

        assertEquals(expected, new LegalRepDetailsMapper().map(bailCase, bailCaseDataMapper));
    }
}
