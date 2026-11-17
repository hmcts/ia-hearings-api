package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingParticipant;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.IndividualDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.OrganisationDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.IndividualDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.OrganisationDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.Party;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartyOneOff;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyNamesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils.convertFromUTC;
import static uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils.convertToLocalStringFormat;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_CASE_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_JUDGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_METHODS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_START_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAPER_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;

@Component
@Slf4j
public class RecordAttendeesAndDurationMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final String SELECT_HEARING_PAGE_ID = "selectHearingRecordAttendeesAndDuration";
    private static final String TIMES_PAGE_ID = "timesRecordAttendeesAndDuration";
    private static final DateTimeFormatter DATE_TIME_LABEL_FORMATTER =
        DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale.UK);

    private final HearingService hearingService;
    private final HearingActualsService hearingActualsService;
    private final PartyNamesMapper partyNamesMapper;

    public RecordAttendeesAndDurationMidEventHandler(
        HearingService hearingService,
        HearingActualsService hearingActualsService,
        PartyNamesMapper partyNamesMapper
    ) {
        this.hearingService = hearingService;
        this.hearingActualsService = hearingActualsService;
        this.partyNamesMapper = partyNamesMapper;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == RECORD_ATTENDEES_AND_DURATION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        String pageId = callback.getPageId();

        log.info("Handling record attendees and duration mid event, pageId {}", pageId);

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (Objects.equals(pageId, SELECT_HEARING_PAGE_ID)) {
            Optional<DynamicList> selectedHearingOptional =
                asylumCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class);
            if (selectedHearingOptional.isPresent()) {
                DynamicList selectedHearing = selectedHearingOptional.get();
                String hearingId = selectedHearing.getValue().getCode();
                HearingDetails hearingDetails = hearingService.getHearing(hearingId).getHearingDetails();
                log.info("hearingDetails {}", hearingDetails);
                HearingActualsData hearingActuals = hearingActualsService.getHearingActuals(hearingId);
                log.info("hearingActuals {}", hearingActuals);

                Optional<PlannedHearingDay> hearingDayOpt =
                    hearingActuals.getHearingPlanned().getPlannedHearingDays().stream().findFirst();
                String hearingDate;
                if (hearingDayOpt.isPresent()) {
                    hearingDate = hearingDayOpt.get().getPlannedStartTime();
                } else {
                    hearingDate = "";
                }

                asylumCase.write(
                    ATTENDEES_AND_DURATION_CASE_REFERENCE, String.valueOf(callback.getCaseDetails().getId()));
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_ID, hearingId);
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_DATE, hearingDate);
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_DATE_DISPLAY, toDateTimeLabel(hearingDate));
                asylumCase.write(
                    ATTENDEES_AND_DURATION_PAPER_HEARING,
                    hearingDetails.getHearingChannels().contains(HearingChannel.ONPPRS.name())
                        ? YesOrNo.YES.toString()
                        : YesOrNo.NO.toString()
                );
                asylumCase.write(
                    ATTENDEES_AND_DURATION_HEARING_METHODS,
                    hearingDetails.getHearingChannels().stream()
                        .map(channelStr -> HearingChannel.valueOf(channelStr).getLabel())
                        .collect(Collectors.joining(", "))
                );
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_STAGE, "");
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_RESULT, "");
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_JUDGE, "");

                List<Value> hearingDateValues = hearingActuals.getHearingPlanned().getPlannedHearingDays().stream()
                        .map(d -> new Value(d.getPlannedStartTime(), toDateLabel(d.getPlannedStartTime())))
                        .collect(Collectors.toList());
                DynamicList hearingDatesList = new DynamicList(new Value("", ""), hearingDateValues);
                asylumCase.write(ATTENDEES_AND_DURATION_DATE_OF_HEARING, hearingDatesList);

                asylumCase.write(ATTENDEES_AND_DURATION_START_TIME, "");
                asylumCase.write(ATTENDEES_AND_DURATION_FINISH_TIME, "");
                asylumCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, "");
            }
        } else if (Objects.equals(pageId, TIMES_PAGE_ID)) {
            asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)
                .filter(hearingId -> !hearingId.isEmpty())
                .ifPresent(hearingId -> populateParticipants(callback.getCaseDetails(), hearingId));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void populateParticipants(CaseDetails<AsylumCase> caseDetails, String hearingId) {
        AsylumCase asylumCase = caseDetails.getCaseData();
        String selectedStartTime = asylumCase.read(ATTENDEES_AND_DURATION_DATE_OF_HEARING, DynamicList.class)
            .map(DynamicList::getValue)
            .map(Value::getCode)
            .orElse("");
        String source = hearingId + "|" + selectedStartTime;
        boolean alreadyPopulated = asylumCase.read(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, String.class)
            .map(source::equals)
            .orElse(false);
        if (alreadyPopulated) {
            // keep the user's edits when they come back through this page for the same hearing day
            return;
        }

        HearingActualsData actuals = hearingActualsService.getHearingActuals(hearingId);
        Optional<PlannedHearingDay> hearingDay = selectedHearingDay(actuals, selectedStartTime);
        hearingDay.ifPresent(day -> populatePartyDetailsFromCaseData(day, caseDetails));
        asylumCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS, toParticipants(hearingDay));
        asylumCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, source);
    }

    private Optional<PlannedHearingDay> selectedHearingDay(
        HearingActualsData actuals,
        String selectedStartTime
    ) {
        List<PlannedHearingDay> days = Optional.ofNullable(actuals.getHearingPlanned())
            .map(HearingPlanned::getPlannedHearingDays)
            .orElse(List.of());

        return days.stream()
            .filter(day -> Objects.equals(day.getPlannedStartTime(), selectedStartTime))
            .findFirst()
            .or(() -> days.stream().findFirst());
    }

    // HMC hearing actuals do not always carry the parties' names, so fill in the missing names of the appellant,
    // legal representative (individual and organisation) and witnesses from the case data
    private void populatePartyDetailsFromCaseData(PlannedHearingDay day, CaseDetails<AsylumCase> caseDetails) {
        List<Party> parties = Optional.ofNullable(day.getParties()).orElse(List.of());
        if (parties.isEmpty()) {
            return;
        }

        List<PartyDetailsModel> caseParties;
        try {
            caseParties = partyNamesMapper.mapAsylumPartyNames(caseDetails.getCaseData());
        } catch (RuntimeException e) {
            log.warn("Unable to read party names from case data for case {}, participants will not be "
                     + "pre-populated with names", caseDetails.getId(), e);
            return;
        }

        for (Party party : parties) {
            findCaseParty(party, caseParties).ifPresent(caseParty -> fillPartyDetails(party, caseParty));
        }
    }

    private Optional<PartyDetailsModel> findCaseParty(Party party, List<PartyDetailsModel> caseParties) {
        Optional<PartyDetailsModel> byId = caseParties.stream()
            .filter(caseParty -> party.getPartyId() != null && party.getPartyId().equals(caseParty.getPartyID()))
            .findFirst();
        if (byId.isPresent()) {
            return byId;
        }
        // fall back to the role for parties a case only has one of: the appellant, and the legal rep's
        // individual and organisation (told apart by party type). Witnesses can only be matched by id.
        String role = toRole(normalise(party.getPartyRole()));
        if (PartyNamesMapper.APPELLANT_ROLE.equals(role)) {
            return onlyOne(caseParties.stream()
                .filter(caseParty -> PartyNamesMapper.APPELLANT_ROLE.equals(caseParty.getPartyRole()))
                .toList());
        }
        if (PartyNamesMapper.LEGAL_REP_ROLE.equals(role)) {
            boolean organisation = isOrganisation(party);
            return onlyOne(caseParties.stream()
                .filter(caseParty -> PartyNamesMapper.LEGAL_REP_ROLE.equals(caseParty.getPartyRole()))
                .filter(caseParty -> organisation == (caseParty.getOrganisationDetails() != null))
                .toList());
        }
        return Optional.empty();
    }

    private static Optional<PartyDetailsModel> onlyOne(List<PartyDetailsModel> candidates) {
        return candidates.size() == 1 ? Optional.of(candidates.get(0)) : Optional.empty();
    }

    private static boolean isOrganisation(Party party) {
        if (party.getPartyType() != null && !party.getPartyType().isBlank()) {
            return PartyType.ORG.getPartyType().equalsIgnoreCase(party.getPartyType().trim());
        }
        return Optional.ofNullable(party.getOneOf()).orElse(List.of()).stream()
            .anyMatch(o -> o.getOrganisationDetails() != null && o.getIndividualDetails() == null);
    }

    private void fillPartyDetails(Party party, PartyDetailsModel caseParty) {
        List<PartyOneOff> oneOf = new ArrayList<>(Optional.ofNullable(party.getOneOf()).orElse(List.of()));

        IndividualDetailsModel caseIndividual = caseParty.getIndividualDetails();
        if (caseIndividual != null) {
            PartyOneOff holder = oneOf.stream()
                .filter(o -> o.getIndividualDetails() != null)
                .findFirst()
                .orElseGet(() -> addOneOff(oneOf));
            IndividualDetails individual = Optional.ofNullable(holder.getIndividualDetails())
                .orElseGet(IndividualDetails::new);
            individual.setTitle(firstNonBlank(individual.getTitle(), caseIndividual.getTitle()));
            individual.setFirstName(firstNonBlank(individual.getFirstName(), caseIndividual.getFirstName()));
            individual.setLastName(firstNonBlank(individual.getLastName(), caseIndividual.getLastName()));
            holder.setIndividualDetails(individual);
        }

        OrganisationDetailsModel caseOrganisation = caseParty.getOrganisationDetails();
        if (caseOrganisation != null) {
            PartyOneOff holder = oneOf.stream()
                .filter(o -> o.getOrganisationDetails() != null)
                .findFirst()
                .orElseGet(() -> addOneOff(oneOf));
            OrganisationDetails organisation = Optional.ofNullable(holder.getOrganisationDetails())
                .orElseGet(OrganisationDetails::new);
            organisation.setName(firstNonBlank(organisation.getName(), caseOrganisation.getName()));
            holder.setOrganisationDetails(organisation);
        }

        party.setOneOf(oneOf);
    }

    private PartyOneOff addOneOff(List<PartyOneOff> oneOf) {
        PartyOneOff created = new PartyOneOff();
        oneOf.add(created);
        return created;
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred != null && !preferred.isBlank() ? preferred : fallback;
    }

    private String normalise(String partyRole) {
        return Optional.ofNullable(partyRole)
            .map(String::trim)
            .map(value -> value.toUpperCase(Locale.UK))
            .filter(value -> !value.isEmpty())
            .orElse(null);
    }

    private List<IdValue<HearingParticipant>> toParticipants(Optional<PlannedHearingDay> day) {
        List<Party> parties = day.map(PlannedHearingDay::getParties).orElse(List.of());
        List<IdValue<HearingParticipant>> participants = new ArrayList<>();
        for (Party party : parties) {
            participants.add(new IdValue<>(String.valueOf(participants.size() + 1), toParticipant(party)));
        }
        return participants;
    }

    private HearingParticipant toParticipant(Party party) {
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

        return HearingParticipant.builder()
            .partyId(party.getPartyId())
            .firstName(individual.map(IndividualDetails::getFirstName).orElse(null))
            .lastName(individual.map(IndividualDetails::getLastName).orElse(null))
            .role(role)
            // fall back to HMC's own code so a pre-filled attendee always shows a role
            .roleLabel(Optional.ofNullable(toRoleLabel(role)).orElse(partyRole))
            .prefilled(YesOrNo.YES)
            .attendanceType(toAttendanceType(party.getPartyChannelSubType()))
            .organisation(organisation.map(OrganisationDetails::getName).orElse(null))
            .build();
    }

    private String toRole(String partyRole) {
        if (partyRole == null) {
            return null;
        }
        return switch (partyRole) {
            case "RESP" -> "RESP";
            case "APEL", "APPL" -> "APEL";
            case "WITN" -> "WITN";
            case "LGRP", "LR" -> "LGRP";
            default -> null;
        };
    }

    private String toRoleLabel(String role) {
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

    private String toAttendanceType(String partyChannelSubType) {
        if (partyChannelSubType == null) {
            return null;
        }
        if (partyChannelSubType.startsWith(HearingChannel.VID.name())) {
            return HearingChannel.VID.name();
        }
        if (partyChannelSubType.startsWith(HearingChannel.TEL.name())) {
            return HearingChannel.TEL.name();
        }
        return Arrays.stream(HearingChannel.values())
            .map(HearingChannel::name)
            .filter(partyChannelSubType::equals)
            .findFirst()
            .orElse(null);
    }

    private String toDateLabel(String plannedStartTime) {
        if (plannedStartTime == null || plannedStartTime.isEmpty()) {
            return "";
        }
        return convertToLocalStringFormat(convertFromUTC(LocalDateTime.parse(plannedStartTime)));
    }

    private String toDateTimeLabel(String plannedStartTime) {
        if (plannedStartTime == null || plannedStartTime.isEmpty()) {
            return "";
        }
        return convertFromUTC(LocalDateTime.parse(plannedStartTime)).format(DATE_TIME_LABEL_FORMATTER);
    }
}
