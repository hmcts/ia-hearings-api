package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_PAPER_J;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME_PAPER_J;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_GIVEN_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_INDIVIDUAL_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_ORGANISATION_PARTY_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.WITNESS_DETAILS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;

/**
 * Reads just the party ids and names of the appellant, legal representative (individual and organisation)
 * and witnesses from asylum case data.
 *
 * <p>Unlike {@link PartyDetailsMapper}, which builds the full HMC payload and fails when any required field is
 * missing, this never throws for missing data: a party is only left out when the case holds nothing about it.
 */
@Component
public class PartyNamesMapper {

    public static final String APPELLANT_ROLE = "APEL";
    public static final String LEGAL_REP_ROLE = "LGRP";
    public static final String WITNESS_ROLE = "WITN";

    public List<PartyDetailsModel> mapAsylumPartyNames(AsylumCase asylumCase) {
        List<PartyDetailsModel> parties = new ArrayList<>();
        appellant(asylumCase).ifPresent(parties::add);
        legalRepIndividual(asylumCase).ifPresent(parties::add);
        legalRepOrganisation(asylumCase).ifPresent(parties::add);
        parties.addAll(witnesses(asylumCase));
        return parties;
    }

    private Optional<PartyDetailsModel> appellant(AsylumCase asylumCase) {
        return individual(
            read(asylumCase, APPELLANT_PARTY_ID),
            APPELLANT_ROLE,
            read(asylumCase, APPELLANT_GIVEN_NAMES),
            read(asylumCase, APPELLANT_FAMILY_NAME)
        );
    }

    private Optional<PartyDetailsModel> legalRepIndividual(AsylumCase asylumCase) {
        // internal (paper) cases hold the legal rep's names in different fields, so look in both
        boolean internal = MapperUtils.isInternalCase(asylumCase);
        String repJourneyFirstName = read(asylumCase, LEGAL_REP_NAME);
        String repJourneyLastName = read(asylumCase, LEGAL_REP_FAMILY_NAME);
        String internalFirstName = read(asylumCase, LEGAL_REP_GIVEN_NAME);
        String internalLastName = read(asylumCase, LEGAL_REP_FAMILY_NAME_PAPER_J);

        return individual(
            read(asylumCase, LEGAL_REP_INDIVIDUAL_PARTY_ID),
            LEGAL_REP_ROLE,
            internal ? firstNonBlank(internalFirstName, repJourneyFirstName)
                : firstNonBlank(repJourneyFirstName, internalFirstName),
            internal ? firstNonBlank(internalLastName, repJourneyLastName)
                : firstNonBlank(repJourneyLastName, internalLastName)
        );
    }

    private Optional<PartyDetailsModel> legalRepOrganisation(AsylumCase asylumCase) {
        boolean internal = MapperUtils.isInternalCase(asylumCase);
        String repJourneyCompany = read(asylumCase, LEGAL_REP_COMPANY);
        String internalCompany = read(asylumCase, LEGAL_REP_COMPANY_PAPER_J);
        String name = internal
            ? firstNonBlank(internalCompany, repJourneyCompany)
            : firstNonBlank(repJourneyCompany, internalCompany);
        String partyId = read(asylumCase, LEGAL_REP_ORGANISATION_PARTY_ID);

        if (name == null) {
            return Optional.empty();
        }
        return Optional.of(PartyDetailsModel.builder()
            .partyID(partyId)
            .partyType(PartyType.ORG.getPartyType())
            .partyRole(LEGAL_REP_ROLE)
            .organisationDetails(OrganisationDetailsModel.builder()
                .organisationType(PartyType.ORG.getPartyType())
                .name(name)
                .build())
            .build());
    }

    private List<PartyDetailsModel> witnesses(AsylumCase asylumCase) {
        Optional<List<IdValue<WitnessDetails>>> witnessDetails = asylumCase.read(WITNESS_DETAILS);
        return witnessDetails.orElse(List.of()).stream()
            .map(IdValue::getValue)
            .filter(Objects::nonNull)
            .map(witness -> individual(
                blankToNull(witness.getWitnessPartyId()),
                WITNESS_ROLE,
                blankToNull(witness.getWitnessName()),
                blankToNull(witness.getWitnessFamilyName())))
            .flatMap(Optional::stream)
            .toList();
    }

    private static Optional<PartyDetailsModel> individual(String partyId, String role,
                                                          String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return Optional.empty();
        }
        return Optional.of(PartyDetailsModel.builder()
            .partyID(partyId)
            .partyType(PartyType.IND.getPartyType())
            .partyRole(role)
            .individualDetails(IndividualDetailsModel.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .build());
    }

    private static String read(AsylumCase asylumCase, AsylumCaseFieldDefinition field) {
        return blankToNull(asylumCase.read(field, String.class).orElse(null));
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return preferred != null ? preferred : fallback;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
