package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingParticipant;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualDayParty;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualIndividualDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActuals;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingOutcome;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingResult;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PauseDateTime;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_START_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_END;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_START;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PARTICIPANTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecordAttendeesAndDurationRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter HOURS_AND_MINUTES_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private final HearingService hearingService;
    private final HearingActualsService hearingActualsService;
    private final UpdateHearingPayloadService updateHearingPayloadService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && RECORD_ATTENDEES_AND_DURATION == callback.getEvent();
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        log.info("Handling RecordAttendeesAndDurationRequest");

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<String> hearingIdOpt = asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID);
        if (hearingIdOpt.isPresent()) {
            HearingOutcome hearingOutcome = new HearingOutcome();

            String hearingDateTime = asylumCase.read(ATTENDEES_AND_DURATION_HEARING_DATE, String.class)
                .filter(dateTime -> !dateTime.isEmpty())
                .orElseGet(() -> readFirstPlannedStartTime(hearingIdOpt.get()));
            String hearingDate =
                hearingDateTime.isEmpty() ? "" : LocalDateTime.parse(hearingDateTime).toLocalDate().toString();
            hearingOutcome.setHearingResultDate(hearingDate);

            String hearingTypeStr = asylumCase.read(ATTENDEES_AND_DURATION_HEARING_STAGE, String.class).orElse("");
            log.info("hearingTypeStr is {}", hearingTypeStr);
            if (HearingType.from(hearingTypeStr).isPresent()) {
                log.info("hearingTypeStr is {}", HearingType.from(hearingTypeStr).get());
                hearingOutcome.setHearingType(HearingType.from(hearingTypeStr).get().getHmcValue());
            } else {
                log.info("hearingTypeStr is {}", "None");
                hearingOutcome.setHearingType("None");
            }
            String hearingResultStr = asylumCase.read(ATTENDEES_AND_DURATION_HEARING_RESULT, String.class).orElse("");
            log.info("hearingResultStr is {}", hearingResultStr);
            if (HearingResult.from(hearingResultStr).isPresent()) {
                HearingResult hearingResult = HearingResult.from(hearingResultStr).get();
                log.info("hearingResultStr is {}", hearingResult);
                hearingOutcome.setHearingResult(hearingResult.getHmcValue());

                if (hearingResult.equals(HearingResult.ADJOURNED)) {
                    if (asylumCase.read(ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON).isPresent()) {
                        hearingOutcome.setHearingResultReasonType(asylumCase.read(
                            ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON,
                            DynamicList.class
                        ).get().getValue().getCode());
                    }
                } else if (hearingResult.equals(HearingResult.CANCELLED)) {
                    if (asylumCase.read(ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON).isPresent()) {
                        hearingOutcome.setHearingResultReasonType(asylumCase.read(
                            ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON,
                            DynamicList.class
                        ).get().getValue().getCode());
                    }
                }
            }

            hearingOutcome.setHearingFinalFlag(false);

            HearingActuals hearingActuals = new HearingActuals();
            hearingActuals.setHearingOutcome(hearingOutcome);

            ActualHearingDay actualHearingDay = new ActualHearingDay();
            actualHearingDay.setHearingDate(hearingDate);

            boolean hearingDayRequired = asylumCase
                .read(ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED, YesOrNo.class)
                .map(YesOrNo.YES::equals)
                .orElse(true);

            if (hearingDayRequired) {
                String hearingStartTime =
                    asylumCase.read(ATTENDEES_AND_DURATION_START_TIME, String.class).orElse("");
                actualHearingDay.setHearingStartTime(toDateTime(hearingDate, hearingStartTime));
                String hearingEndTime =
                    asylumCase.read(ATTENDEES_AND_DURATION_FINISH_TIME, String.class).orElse("");
                actualHearingDay.setHearingEndTime(toDateTime(hearingDate, hearingEndTime));

                actualHearingDay.setPauseDateTimes(readPauseDateTimes(asylumCase, hearingDate));
            } else {
                actualHearingDay.setPauseDateTimes(Collections.emptyList());
            }

            actualHearingDay.setActualDayParties(readActualDayParties(asylumCase));

            actualHearingDay.setNotRequired(false);
            actualHearingDay.setNotRequired(!hearingDayRequired);

            Set<ActualHearingDay> actualHearingDays = new HashSet<>();
            actualHearingDays.add(actualHearingDay);

            String hearingId = hearingIdOpt.get();
            hearingActuals.setActualHearingDays(actualHearingDays);

            log.info("Updating hearing actuals {} for hearing id {}", hearingActuals, hearingId);
            hearingActualsService.updateHearingActuals(hearingActuals, hearingId);
            log.info("Updated hearing actuals for hearing id {}", hearingId);
            hearingActualsService.completeHearingActuals(hearingId);
            log.info("Completed hearing actuals for hearing id {}", hearingId);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String readFirstPlannedStartTime(String hearingId) {
        return Optional.ofNullable(hearingActualsService.getHearingActuals(hearingId))
            .map(HearingActualsData::getHearingPlanned)
            .map(HearingPlanned::getPlannedHearingDays)
            .flatMap(days -> days.stream().findFirst())
            .map(PlannedHearingDay::getPlannedStartTime)
            .orElse("");
    }

    private static List<ActualDayParty> readActualDayParties(AsylumCase asylumCase) {
        List<HearingParticipant> participants =
            asylumCase.<List<IdValue<HearingParticipant>>>read(ATTENDEES_AND_DURATION_PARTICIPANTS)
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .toList();

        // HMC needs an id for every party, so attendees added on the page get a new one
        List<String> partyIds = participants.stream()
            .map(participant -> Optional.ofNullable(blankToNull(participant.getPartyId()))
                .orElseGet(() -> UUID.randomUUID().toString()))
            .toList();

        // HMC expects representedParty to be the id of a party in this list, so the selected role
        // becomes the id of the first attendee with that role
        Map<String, String> partyIdByRole = new HashMap<>();
        for (int i = 0; i < participants.size(); i++) {
            String role = toHmcPartyRole(participants.get(i).getRole());
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

    private static ActualDayParty toActualDayParty(
        HearingParticipant participant,
        String partyId,
        Map<String, String> partyIdByRole
    ) {
        boolean didNotAttend = HearingChannel.NA.name().equals(participant.getAttendanceType());
        boolean hasName = isNotBlank(participant.getFirstName()) || isNotBlank(participant.getLastName());

        return ActualDayParty.builder()
            .actualPartyId(partyId)
            .partyRole(toHmcPartyRole(participant.getRole()))
            .individualDetails(hasName
                ? new ActualIndividualDetails(participant.getFirstName(), participant.getLastName())
                : null)
            .actualOrganisationName(blankToNull(participant.getOrganisation()))
            .partyChannelSubType(didNotAttend ? null : participant.getAttendanceType())
            .didNotAttendFlag(didNotAttend)
            .representedParty(readRepresentedParty(participant, partyId, partyIdByRole))
            .build();
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
        return Optional.ofNullable(blankToNull(participant.getRepresentedParty()))
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

    private static List<PauseDateTime> readPauseDateTimes(AsylumCase asylumCase, String hearingDate) {
        boolean pausedTimeRequired = asylumCase.read(ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED, YesOrNo.class)
            .map(YesOrNo.YES::equals)
            .orElse(false);
        if (!pausedTimeRequired) {
            return Collections.emptyList();
        }

        String pauseStart = asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_START, String.class).orElse("");
        String pauseEnd = asylumCase.read(ATTENDEES_AND_DURATION_PAUSE_END, String.class).orElse("");
        if (pauseStart.isEmpty() || pauseEnd.isEmpty()) {
            return Collections.emptyList();
        }

        return List.of(new PauseDateTime(toDateTime(hearingDate, pauseStart), toDateTime(hearingDate, pauseEnd)));
    }

    private static String toDateTime(String date, String time) {
        if (date.isEmpty() || time.isEmpty()) {
            return "";
        }
        String hoursAndMinutes = time.contains(":") ? time : time + ":00";
        return LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(hoursAndMinutes, HOURS_AND_MINUTES_FORMATTER))
            .format(DATE_TIME_FORMATTER);
    }
}
