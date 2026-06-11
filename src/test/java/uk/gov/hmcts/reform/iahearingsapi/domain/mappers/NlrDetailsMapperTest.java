package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@ExtendWith(MockitoExtension.class)
class NlrDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private HearingDetails persistedHearingDetails;
    @Mock
    private NonLegalRepDetails nlrDetails;
    @Mock
    private Event event;

    @Test
    void should_map_correctly_with_phone_number() {
        when(caseDataMapper.getHearingChannel(asylumCase, persistedHearingDetails, event))
            .thenReturn("hearingChannel");
        String idamId = "idamId";
        String emailAddress = "emailAddress";
        String givenNames = "givenNames";
        String familyName = "familyName";
        String phoneNumber = "phoneNumber";
        when(nlrDetails.getIdamId()).thenReturn(idamId);
        when(nlrDetails.getEmailAddress()).thenReturn(emailAddress);
        when(nlrDetails.getGivenNames()).thenReturn(givenNames);
        when(nlrDetails.getFamilyName()).thenReturn(familyName);
        when(nlrDetails.getPhoneNumber()).thenReturn(phoneNumber);

        PartyDetailsModel actual = new NlrDetailsMapper()
            .map(asylumCase, nlrDetails, caseDataMapper, persistedHearingDetails, event);

        assertEquals(idamId, actual.getPartyID());
        assertEquals("IND", actual.getPartyType());
        assertEquals("NLRP", actual.getPartyRole());
        IndividualDetailsModel individualDetails = actual.getIndividualDetails();
        assertEquals(givenNames, individualDetails.getFirstName());
        assertEquals(familyName, individualDetails.getLastName());
        assertEquals("hearingChannel", individualDetails.getPreferredHearingChannel());
        assertFalse(individualDetails.getHearingChannelEmail().isEmpty());
        assertFalse(individualDetails.getHearingChannelPhone().isEmpty());
        assertEquals(emailAddress, individualDetails.getHearingChannelEmail().get(0));
        assertEquals(phoneNumber, individualDetails.getHearingChannelPhone().get(0));
    }


    @Test
    void should_map_correctly_without_phone_number() {
        when(caseDataMapper.getHearingChannel(asylumCase, persistedHearingDetails, event))
            .thenReturn("hearingChannel");
        String idamId = "idamId";
        String emailAddress = "emailAddress";
        String givenNames = "givenNames";
        String familyName = "familyName";
        when(nlrDetails.getIdamId()).thenReturn(idamId);
        when(nlrDetails.getEmailAddress()).thenReturn(emailAddress);
        when(nlrDetails.getGivenNames()).thenReturn(givenNames);
        when(nlrDetails.getFamilyName()).thenReturn(familyName);
        when(nlrDetails.getPhoneNumber()).thenReturn(null);

        PartyDetailsModel actual = new NlrDetailsMapper()
            .map(asylumCase, nlrDetails, caseDataMapper, persistedHearingDetails, event);

        assertEquals(idamId, actual.getPartyID());
        assertEquals("IND", actual.getPartyType());
        assertEquals("NLRP", actual.getPartyRole());
        IndividualDetailsModel individualDetails = actual.getIndividualDetails();
        assertEquals(givenNames, individualDetails.getFirstName());
        assertEquals(familyName, individualDetails.getLastName());
        assertEquals("hearingChannel", individualDetails.getPreferredHearingChannel());
        assertFalse(individualDetails.getHearingChannelEmail().isEmpty());
        assertTrue(individualDetails.getHearingChannelPhone().isEmpty());
        assertEquals(emailAddress, individualDetails.getHearingChannelEmail().get(0));
    }
}
