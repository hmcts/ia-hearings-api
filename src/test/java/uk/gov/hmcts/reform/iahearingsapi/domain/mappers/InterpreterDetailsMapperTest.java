package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.INTERPRETER_DETAILS;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.InterpreterDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

@ExtendWith(MockitoExtension.class)
class InterpreterDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;

    @Test
    void testMapWithInterpreterDetails() {
        InterpreterDetails interpreterDetails1 = createInterpreterDetails("INTP1");
        InterpreterDetails interpreterDetails2 = createInterpreterDetails("INTP2");

        List<IdValue<InterpreterDetails>> interpreterDetailsList = Arrays.asList(
            new IdValue<>("1", interpreterDetails1),
            new IdValue<>("2", interpreterDetails2)
        );

        when(asylumCase.read(INTERPRETER_DETAILS)).thenReturn(Optional.of(interpreterDetailsList));

        when(caseDataMapper.getHearingChannel(asylumCase)).thenReturn("Email");

        InterpreterDetailsMapper interpreterDetailsMapper = new InterpreterDetailsMapper();
        List<PartyDetailsModel> partyDetailsList = interpreterDetailsMapper.map(asylumCase, caseDataMapper);

        assertEquals(2, partyDetailsList.size());

        PartyDetailsModel partyDetails1 = partyDetailsList.get(0);
        assertEquals("INTP1", partyDetails1.getPartyID());
        assertEquals("IND", partyDetails1.getPartyType());
        assertEquals("INTP", partyDetails1.getPartyRole());
        assertEquals("Email", partyDetails1.getIndividualDetails().getPreferredHearingChannel());
        assertEquals(
            Collections.singletonList(interpreterDetails1.getInterpreterEmail()),
            partyDetails1.getIndividualDetails().getHearingChannelEmail()
        );

        PartyDetailsModel partyDetails2 = partyDetailsList.get(1);
        assertEquals("INTP2", partyDetails2.getPartyID());
        assertEquals("IND", partyDetails2.getPartyType());
        assertEquals("INTP", partyDetails2.getPartyRole());
        assertEquals("Email", partyDetails2.getIndividualDetails().getPreferredHearingChannel());
        assertEquals(
            Collections.singletonList(interpreterDetails2.getInterpreterEmail()),
            partyDetails2.getIndividualDetails().getHearingChannelEmail()
        );
    }

    @Test
    void testMapWithNoInterpreterDetails() {
        when(asylumCase.read(INTERPRETER_DETAILS)).thenReturn(Optional.empty());

        InterpreterDetailsMapper interpreterDetailsMapper = new InterpreterDetailsMapper();
        List<PartyDetailsModel> partyDetailsList = interpreterDetailsMapper.map(asylumCase, caseDataMapper);

        assertTrue(partyDetailsList.isEmpty());
    }

    private InterpreterDetails createInterpreterDetails(String id) {
        return new InterpreterDetails(
            id,
            "BookingRef",
            "GivenName",
            "FamilyName",
            "PhoneNumber",
            "Email",
            "InterpreterNote"
        );
    }
}