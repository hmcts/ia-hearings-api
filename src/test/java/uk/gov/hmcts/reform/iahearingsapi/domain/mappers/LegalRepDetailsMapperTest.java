package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_NAME;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.bail.BailCaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

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
    @Mock
    private HearingDetails persistedHearingDetails;
    @Mock
    Event event;

    @ParameterizedTest
    @CsvSource({
        "firstName middleName familyName, , firstName middleName, familyName",
        "firstName familyName, , firstName, familyName",
        "firstName, , firstName, ",
        ", , , ",
        "firstName middleName, familyName, firstName middleName, familyName",
        "firstName middleName, familyName familySecondName, firstName middleName, familyName familySecondName",
        "firstName, familyName, firstName, familyName",

    })
    void should_map_asylum_correctly(String legalRepName, String legalRepFamilyName,
                                     String expectedGivenNames, String expectedLastName) {

        when(caseDataMapper.getLegalRepPartyId(asylumCase)).thenReturn("partyId");
        when(caseDataMapper.getHearingChannel(asylumCase, persistedHearingDetails, event))
            .thenReturn("hearingChannel");
        when(caseDataMapper.getName(asylumCase, LEGAL_REP_NAME)).thenReturn(legalRepName);
        when(caseDataMapper.getName(asylumCase, LEGAL_REP_FAMILY_NAME)).thenReturn(legalRepFamilyName);
        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .preferredHearingChannel("hearingChannel")
            .firstName(expectedGivenNames)
            .lastName(expectedLastName)
            .build();
        PartyDetailsModel expected = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("LGRP")
            .build();

        assertEquals(expected, new LegalRepDetailsMapper().map(
            asylumCase,
            caseDataMapper,
            persistedHearingDetails,
            event
        ));
    }

    @Test
    void should_map_bail_correctly() {
        when(bailCaseDataMapper.getLegalRepPartyId(bailCase)).thenReturn("partyId");
        when(bailCaseDataMapper.getHearingChannel(bailCase)).thenReturn("VID");
        when(bailCaseDataMapper.getStringValueByDefinition(
            bailCase,
            BailCaseFieldDefinition.LEGAL_REP_NAME
        )).thenReturn("firstName");
        when(bailCaseDataMapper.getStringValueByDefinition(
            bailCase,
            BailCaseFieldDefinition.LEGAL_REP_FAMILY_NAME
        )).thenReturn("lastName");
        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .preferredHearingChannel("VID")
            .firstName("firstName")
            .lastName("lastName")
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
