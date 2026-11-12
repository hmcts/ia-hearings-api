package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingParticipant;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.IndividualDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.OrganisationDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.Party;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartyOneOff;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
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

    public RecordAttendeesAndDurationMidEventHandler(
        HearingService hearingService,
        HearingActualsService hearingActualsService
    ) {
        this.hearingService = hearingService;
        this.hearingActualsService = hearingActualsService;
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
                .ifPresent(hearingId -> populateParticipants(asylumCase, hearingId));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void populateParticipants(AsylumCase asylumCase, String hearingId) {
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
        asylumCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS,
            toParticipants(selectedHearingDay(actuals, selectedStartTime)));
        asylumCase.write(ATTENDEES_AND_DURATION_PARTICIPANTS_SOURCE, source);
    }

    private static Optional<PlannedHearingDay> selectedHearingDay(
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

    private static List<IdValue<HearingParticipant>> toParticipants(Optional<PlannedHearingDay> day) {
        List<Party> parties = day.map(PlannedHearingDay::getParties).orElse(List.of());
        List<IdValue<HearingParticipant>> participants = new ArrayList<>();
        for (Party party : parties) {
            participants.add(new IdValue<>(String.valueOf(participants.size() + 1), toParticipant(party)));
        }
        return participants;
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

        return HearingParticipant.builder()
            .partyId(party.getPartyId())
            .firstName(individual.map(IndividualDetails::getFirstName).orElse(null))
            .lastName(individual.map(IndividualDetails::getLastName).orElse(null))
            .role(toRole(party.getPartyRole()))
            .attendanceType(toAttendanceType(party.getPartyChannelSubType()))
            .organisation(organisation.map(OrganisationDetails::getName).orElse(null))
            .build();
    }

    private static String toRole(String partyRole) {
        if (partyRole == null) {
            return null;
        }
        return switch (partyRole) {
            case "RESP" -> "RESP";
            case "APEL", "APPL" -> "APEL";
            case "WITN" -> "WITN";
            case "LGRP", "LR" -> "LR";
            default -> null;
        };
    }

    private static String toAttendanceType(String partyChannelSubType) {
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

    private static String toDateLabel(String plannedStartTime) {
        if (plannedStartTime == null || plannedStartTime.isEmpty()) {
            return "";
        }
        return convertToLocalStringFormat(convertFromUTC(LocalDateTime.parse(plannedStartTime)));
    }

    private static String toDateTimeLabel(String plannedStartTime) {
        if (plannedStartTime == null || plannedStartTime.isEmpty()) {
            return "";
        }
        return convertFromUTC(LocalDateTime.parse(plannedStartTime)).format(DATE_TIME_LABEL_FORMATTER);
    }
}
