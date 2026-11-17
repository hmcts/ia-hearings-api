package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;

class PartyNamesMapperTest {

    private final PartyNamesMapper mapper = new PartyNamesMapper();

    @Test
    void should_map_appellant_internal_case_legal_rep_and_witnesses() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.put("isAdmin", "Yes");
        asylumCase.put("appellantPartyId", "0654ed2f-465c-4748-b9d1-f3e62379ec1b");
        asylumCase.put("appellantGivenNames", "José");
        asylumCase.put("appellantFamilyName", "González");
        asylumCase.put("legalRepIndividualPartyId", "ec8b105f-7262-49aa-a12d-37e24a7e22c9");
        asylumCase.put("legalRepOrganisationPartyId", "0b37fba3-f3ee-4b05-a6b3-3b27adc06187");
        asylumCase.put("legalRepGivenName", "Stephen");
        asylumCase.put("legalRepFamilyName", "");
        asylumCase.put("legalRepFamilyNamePaperJ", "Fenn");
        asylumCase.put("legalRepCompanyPaperJ", "IA Legal Services");
        asylumCase.put("witnessDetails", List.of(
            witness("1", "d1ed707b-6cd5-4045-900c-7215226c8eaa", "Jenny", "Bat"),
            witness("2", "8c3f3bfc-6f02-4545-a3af-53db12fae1f7", "Krish", "Patel")
        ));

        assertEquals(List.of(
            individual("0654ed2f-465c-4748-b9d1-f3e62379ec1b", "APEL", "José", "González"),
            individual("ec8b105f-7262-49aa-a12d-37e24a7e22c9", "LGRP", "Stephen", "Fenn"),
            organisation("0b37fba3-f3ee-4b05-a6b3-3b27adc06187", "IA Legal Services"),
            individual("d1ed707b-6cd5-4045-900c-7215226c8eaa", "WITN", "Jenny", "Bat"),
            individual("8c3f3bfc-6f02-4545-a3af-53db12fae1f7", "WITN", "Krish", "Patel")
        ), mapper.mapAsylumPartyNames(asylumCase));
    }

    @Test
    void should_map_legal_rep_journey_names() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.put("legalRepIndividualPartyId", "lr-id");
        asylumCase.put("legalRepOrganisationPartyId", "lr-org-id");
        asylumCase.put("legalRepName", "Ann");
        asylumCase.put("legalRepFamilyName", "Lee");
        asylumCase.put("legalRepGivenName", "Ignored");
        asylumCase.put("legalRepCompany", "Lee Solicitors");
        asylumCase.put("legalRepCompanyPaperJ", "Ignored Ltd");

        assertEquals(List.of(
            individual("lr-id", "LGRP", "Ann", "Lee"),
            organisation("lr-org-id", "Lee Solicitors")
        ), mapper.mapAsylumPartyNames(asylumCase));
    }

    @Test
    void should_leave_out_parties_without_names_and_never_fail_on_missing_ids() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.put("appellantGivenNames", "José");
        asylumCase.put("legalRepIndividualPartyId", "lr-id");
        asylumCase.put("witnessDetails", List.of(witness("1", "w-id", " ", "")));

        assertEquals(
            List.of(individual(null, "APEL", "José", null)),
            mapper.mapAsylumPartyNames(asylumCase)
        );
    }

    @Test
    void should_return_empty_list_for_empty_case() {
        assertEquals(List.of(), mapper.mapAsylumPartyNames(new AsylumCase()));
    }

    private static Map<String, Object> witness(String id, String partyId, String name, String familyName) {
        return Map.of("id", id, "value", Map.of(
            "witnessPartyId", partyId,
            "witnessName", name,
            "witnessFamilyName", familyName
        ));
    }

    private static PartyDetailsModel individual(String partyId, String role, String firstName, String lastName) {
        return PartyDetailsModel.builder()
            .partyID(partyId)
            .partyType("IND")
            .partyRole(role)
            .individualDetails(IndividualDetailsModel.builder().firstName(firstName).lastName(lastName).build())
            .build();
    }

    private static PartyDetailsModel organisation(String partyId, String name) {
        return PartyDetailsModel.builder()
            .partyID(partyId)
            .partyType("ORG")
            .partyRole("LGRP")
            .organisationDetails(OrganisationDetailsModel.builder().organisationType("ORG").name(name).build())
            .build();
    }
}
