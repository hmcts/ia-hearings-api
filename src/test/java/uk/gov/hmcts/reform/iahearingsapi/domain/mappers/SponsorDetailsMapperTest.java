package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@ExtendWith(MockitoExtension.class)
class SponsorDetailsMapperTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDataToServiceHearingValuesMapper caseDataMapper;
    @Mock
    private HearingDetails persistedHearingDetails;
    @Mock
    Event event;

    @Test
    void should_map_correctly() {

        when(caseDataMapper.getSponsorPartyId(asylumCase)).thenReturn("partyId");
        when(caseDataMapper.getHearingChannel(asylumCase, persistedHearingDetails, event))
            .thenReturn("hearingChannel");

        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .hearingChannelEmail(Collections.emptyList())
            .hearingChannelPhone(Collections.emptyList())
            .preferredHearingChannel("hearingChannel")
            .build();
        PartyDetailsModel expected = PartyDetailsModel.builder()
            .individualDetails(individualDetails)
            .partyID("partyId")
            .partyType("IND")
            .partyRole("SPON")
            .build();

        assertEquals(expected, new SponsorDetailsMapper().map(asylumCase,
                                                              caseDataMapper,
                                                              persistedHearingDetails,
                                                              event));
    }
}
