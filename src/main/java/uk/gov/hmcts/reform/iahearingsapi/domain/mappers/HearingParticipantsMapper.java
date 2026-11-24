package uk.gov.hmcts.reform.iahearingsapi.domain.mappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingParticipant;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualDayParty;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualIndividualDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.IndividualDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.OrganisationDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.Party;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartyOneOff;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;

/**
 * Maps between HMC's hearing parties and the attendees recorded on the attendees and duration pages,
 * shared by the Asylum (recordAttendeesAndDuration) and Bail (recordTheDecision) events.
 */
public final class HearingParticipantsMapper {

    private HearingParticipantsMapper() {
    }

    /**
     * The individuals planned to attend the chosen hearing day, pre-filled from HMC.
     *
     * @param actuals           HMC's hearing actuals for the hearing
     * @param selectedStartTime the planned start time of the chosen hearing day; the first day is used if no day
     *                          matches
     * @return the attendees, without organisations
     */
    public static List<IdValue<HearingParticipant>> toParticipants(
        HearingActualsData actuals,
        String selectedStartTime
    ) {
        List<Party> parties = selectedHearingDay(actuals, selectedStartTime)
            .map(PlannedHearingDay::getParties)
            .orElse(List.of());
        List<IdValue<HearingParticipant>> participants = new ArrayList<>();
        for (Party party : parties) {
            // only individuals attend; organisations (e.g. the Home Office, a legal rep's firm) are not listed
            if (isOrganisation(party)) {
                continue;
            }
            participants.add(new IdValue<>(String.valueOf(participants.size() + 1), toParticipant(party)));
        }
        return participants;
    }

    /**
     * The attendees recorded on the page, as HMC's actual day parties.
     *
     * @param participants the attendees recorded on the page
     * @return the parties to send to HMC with the hearing actuals
     */
    public static List<ActualDayParty> toActualDayParties(List<HearingParticipant> participants) {
        List<String> partyIds = participants.stream()
            .map(participant -> Optional.ofNullable(blankToNull(participant.getPartyId()))
                .orElseGet(() -> UUID.randomUUID().toString()))
            .toList();

        Map<String, String> partyIdByRole = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            String role = toHmcPartyRole(code(participants.get(i).getRole()));
            if (role != null) {
                partyIdByRole.putIfAbsent(role, partyIds.get(i));
            }
        }

        List<ActualDayParty> parties = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            parties.add(toActualDayParty(participants.get(i), partyIds.get(i), partyIdByRole));
        }
        return parties;
    }

    private static Optional<PlannedHearingDay> selectedHearingDay(
        HearingActualsData actuals,
        String selectedStartTime
    ) {
        List<PlannedHearingDay> days = Optional.ofNullable(actuals)
            .map(HearingActualsData::getHearingPlanned)
            .map(HearingPlanned::getPlannedHearingDays)
            .orElse(List.of());

        return days.stream()
            .filter(day -> Objects.equals(day.getPlannedStartTime(), selectedStartTime))
            .findFirst()
            .or(() -> days.stream().findFirst());
    }

    private static boolean isOrganisation(Party party) {
        if (party.getPartyType() != null && !party.getPartyType().isBlank()) {
            return PartyType.ORG.getPartyType().equalsIgnoreCase(party.getPartyType().trim());
        }
        return Optional.ofNullable(party.getOneOf()).orElse(List.of()).stream()
            .anyMatch(o -> o.getOrganisationDetails() != null && o.getIndividualDetails() == null);
    }

    private static HearingParticipant toParticipant(Party party) {
        List<PartyOneOff> details = Optional.ofNullable(party.getOneOf()).orElse(List.of());
        Optional<IndividualDetails> individual = details.stream()
            .map(PartyOneOff::getIndividualDetails)
            .filter(Objects::nonNull)
            .findFirst();
        Optional<OrganisationDetails> organisation = details.stream()
            .map(PartyOneOff::getOrganisationDetails)
            .filter(Objects::nonNull)
            .findFirst();

        String partyRole = normalise(party.getPartyRole());
        String role = toRole(partyRole);
        String roleLabel = Optional.ofNullable(toRoleLabel(role)).orElse(partyRole);

        String firstName = individual.map(IndividualDetails::getFirstName).orElse(null);
        String lastName = individual.map(IndividualDetails::getLastName).orElse(null);

        return HearingParticipant.builder()
            .partyId(party.getPartyId())
            .firstName(firstName)
            .lastName(lastName)
            .firstNameLabel(firstName)
            .lastNameLabel(lastName)
            .role(role == null ? null : new DynamicList(new Value(role, roleLabel), List.of()))
            .roleLabel(roleLabel)
            .prefilled(YesOrNo.YES)
            .attendanceType(toAttendanceType(party.getPartyChannelSubType()))
            .organisation(organisation.map(OrganisationDetails::getName).orElse(null))
            .build();
    }

    private static String normalise(String partyRole) {
        return Optional.ofNullable(partyRole)
            .map(String::trim)
            .map(value -> value.toUpperCase(Locale.UK))
            .filter(value -> !value.isEmpty())
            .orElse(null);
    }

    private static String toRole(String partyRole) {
        if (partyRole == null) {
            return null;
        }
        // HMC's EntityRoleCode, with IA's older codes mapped to the ones used in ref data
        return switch (partyRole) {
            case "APPL" -> "APEL";
            case "LR" -> "LGRP";
            default -> partyRole;
        };
    }

    private static String toRoleLabel(String role) {
        if (role == null) {
            return null;
        }
        return switch (role) {
            case "RESP" -> "Respondent";
            case "APEL" -> "Appellant";
            case "WITN" -> "Witness";
            case "LGRP" -> "Legal Representative";
            default -> null;
        };
    }

    private static DynamicList toAttendanceType(String partyChannelSubType) {
        // preselects HMC's sub-channel (e.g. TELCVP); the case API adds the ref data list items and label
        return Optional.ofNullable(partyChannelSubType)
            .map(String::trim)
            .filter(code -> !code.isEmpty())
            .map(code -> new DynamicList(new Value(code, code), List.of()))
            .orElse(null);
    }

    private static ActualDayParty toActualDayParty(
        HearingParticipant participant,
        String partyId,
        Map<String, String> partyIdByRole
    ) {
        String channel = code(participant.getAttendanceType());
        boolean didNotAttend = HearingChannel.NA.name().equals(channel);
        boolean hasName = isNotBlank(participant.getFirstName()) || isNotBlank(participant.getLastName());

        return ActualDayParty.builder()
            .actualPartyId(partyId)
            .partyRole(toHmcPartyRole(code(participant.getRole())))
            .individualDetails(hasName
                ? new ActualIndividualDetails(participant.getFirstName(), participant.getLastName())
                : null)
            .actualOrganisationName(blankToNull(participant.getOrganisation()))
            .partyChannelSubType(channel)
            .didNotAttendFlag(didNotAttend)
            .representedParty(readRepresentedParty(participant, partyId, partyIdByRole))
            .build();
    }

    private static String code(DynamicList list) {
        return Optional.ofNullable(list)
            .map(DynamicList::getValue)
            .map(Value::getCode)
            .map(String::trim)
            .filter(code -> !code.isEmpty())
            .orElse(null);
    }

    private static String readRepresentedParty(
        HearingParticipant participant,
        String partyId,
        Map<String, String> partyIdByRole
    ) {
        // only attendees added on the page choose who they represent; pre-filled ones come from HMC as is
        if (participant.getPrefilled() == YesOrNo.YES) {
            return null;
        }
        return Optional.ofNullable(code(participant.getRepresentedParty()))
            .map(code -> partyIdByRole.get(toHmcPartyRole(code)))
            .filter(representedId -> !representedId.equals(partyId))
            .orElse(null);
    }

    private static String toHmcPartyRole(String role) {
        // IA sends legal representatives to HMC with the LGRP role code
        return "LR".equals(role) ? "LGRP" : role;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToNull(String value) {
        return isNotBlank(value) ? value : null;
    }
}
