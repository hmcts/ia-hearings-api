package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

class PartyTest {

    // configured as the Feign decoder used for HMC responses
    private final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void should_read_party_id_and_top_level_details_sent_by_hmc() throws Exception {
        String json = """
            {
              "plannedStartTime": "2026-11-17T09:45:00",
              "parties": [
                {
                  "partyID": "d1ed707b-6cd5-4045-900c-7215226c8eaa",
                  "partyRole": "WITN",
                  "individualDetails": { "title": null, "firstName": null, "lastName": null },
                  "partyChannelSubType": "INTER"
                },
                {
                  "partyID": "0b37fba3-f3ee-4b05-a6b3-3b27adc06187",
                  "partyRole": "LGRP",
                  "organisationDetails": { "name": "IA Legal Services" },
                  "partyChannelSubType": "INTER"
                }
              ]
            }
            """;

        List<Party> parties = objectMapper.readValue(json, PlannedHearingDay.class).getParties();

        Party witness = parties.get(0);
        assertEquals("d1ed707b-6cd5-4045-900c-7215226c8eaa", witness.getPartyId());
        assertEquals("WITN", witness.getPartyRole());
        assertEquals(List.of(PartyOneOff.builder().individualDetails(new IndividualDetails()).build()),
            witness.getOneOf());

        Party legalRepOrganisation = parties.get(1);
        assertEquals("0b37fba3-f3ee-4b05-a6b3-3b27adc06187", legalRepOrganisation.getPartyId());
        assertEquals(List.of(PartyOneOff.builder()
                .organisationDetails(OrganisationDetails.builder().name("IA Legal Services").build())
                .build()),
            legalRepOrganisation.getOneOf());
    }

    @Test
    void should_still_read_party_id_and_one_of_list() throws Exception {
        String json = """
            {
              "partyId": "p1",
              "oneOf": [ { "individualDetails": { "firstName": "Jenny", "lastName": "Bat" } } ]
            }
            """;

        Party party = objectMapper.readValue(json, Party.class);

        assertEquals("p1", party.getPartyId());
        assertEquals(List.of(PartyOneOff.builder()
                .individualDetails(IndividualDetails.builder().firstName("Jenny").lastName("Bat").build())
                .build()),
            party.getOneOf());
    }

    @Test
    void should_leave_one_of_empty_when_no_details_sent() throws Exception {
        Party party = objectMapper.readValue("{\"partyID\": \"p1\", \"individualDetails\": null}", Party.class);

        assertEquals("p1", party.getPartyId());
        assertNull(party.getOneOf());
    }
}
