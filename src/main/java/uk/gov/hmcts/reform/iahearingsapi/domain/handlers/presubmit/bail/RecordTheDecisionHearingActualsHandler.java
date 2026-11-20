package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.bail;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_END;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_PAUSE_START;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.ATTENDEES_AND_DURATION_START_TIME;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActuals;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingOutcome;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingPlanned;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingResult;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PauseDateTime;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;

/**
 * Sends the hearing details recorded with a bail decision to HMC as the hearing actuals.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RecordTheDecisionHearingActualsHandler implements PreSubmitCallbackHandler<BailCase> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter HOURS_AND_MINUTES_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private final HearingActualsService hearingActualsService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RECORD_THE_DECISION;
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        Optional<String> hearingIdOpt = readHearingId(bailCase);
        if (hearingIdOpt.isEmpty()) {
            log.info("No hearing selected for case {}, hearing actuals not sent",
                     callback.getCaseDetails().getId());
            return new PreSubmitCallbackResponse<>(bailCase);
        }
        String hearingId = hearingIdOpt.get();

        String hearingDate = readHearingDate(bailCase, hearingId);
        boolean hearingDayRequired = bailCase.read(ATTENDEES_AND_DURATION_HEARING_DAY_REQUIRED, YesOrNo.class)
            .map(YesOrNo.YES::equals)
            .orElse(true);

        ActualHearingDay actualHearingDay = new ActualHearingDay();
        actualHearingDay.setHearingDate(hearingDate);
        if (hearingDayRequired) {
            actualHearingDay.setHearingStartTime(
                toDateTime(hearingDate, read(bailCase, ATTENDEES_AND_DURATION_START_TIME)));
            actualHearingDay.setHearingEndTime(
                toDateTime(hearingDate, read(bailCase, ATTENDEES_AND_DURATION_FINISH_TIME)));
            actualHearingDay.setPauseDateTimes(readPauseDateTimes(bailCase, hearingDate));
        } else {
            actualHearingDay.setPauseDateTimes(List.of());
        }
        // attendees are not recorded for bail hearings
        actualHearingDay.setActualDayParties(List.of());
        actualHearingDay.setNotRequired(!hearingDayRequired);

        HearingActuals hearingActuals = new HearingActuals();
        hearingActuals.setHearingOutcome(toHearingOutcome(bailCase, hearingDate));
        hearingActuals.setActualHearingDays(Set.of(actualHearingDay));

        log.info("Updating hearing actuals for hearing id {}", hearingId);
        hearingActualsService.updateHearingActuals(hearingActuals, hearingId);
        hearingActualsService.completeHearingActuals(hearingId);
        log.info("Completed hearing actuals for hearing id {}", hearingId);

        return new PreSubmitCallbackResponse<>(bailCase);
    }

    private static Optional<String> readHearingId(BailCase bailCase) {
        return bailCase.read(ATTENDEES_AND_DURATION_HEARING_ID, String.class)
            .filter(id -> !id.isBlank())
            // the hearing ID is shown read-only, so the selected list item is used if it is not sent back
            .or(() -> selectedCode(bailCase, ATTENDEES_AND_DURATION_HEARING));
    }

    private String readHearingDate(BailCase bailCase, String hearingId) {
        String hearingDateTime = selectedCode(bailCase, ATTENDEES_AND_DURATION_DATE_OF_HEARING)
            .orElseGet(() -> readFirstPlannedStartTime(hearingId));

        return hearingDateTime.isEmpty() ? "" : LocalDateTime.parse(hearingDateTime).toLocalDate().toString();
    }

    private String readFirstPlannedStartTime(String hearingId) {
        return Optional.ofNullable(hearingActualsService.getHearingActuals(hearingId))
            .map(HearingActualsData::getHearingPlanned)
            .map(HearingPlanned::getPlannedHearingDays)
            .flatMap(days -> days.stream().findFirst())
            .map(PlannedHearingDay::getPlannedStartTime)
            .orElse("");
    }

    private static HearingOutcome toHearingOutcome(BailCase bailCase, String hearingDate) {
        HearingOutcome hearingOutcome = new HearingOutcome();
        hearingOutcome.setHearingResultDate(hearingDate);
        // the hearing stage list holds HMC's hearing type codes
        hearingOutcome.setHearingType(
            Optional.of(read(bailCase, ATTENDEES_AND_DURATION_HEARING_STAGE))
                .filter(stage -> !stage.isBlank())
                .orElse(HearingType.BAIL.getKey())
        );
        hearingOutcome.setHearingFinalFlag(false);

        HearingResult.from(read(bailCase, ATTENDEES_AND_DURATION_HEARING_RESULT)).ifPresent(hearingResult -> {
            hearingOutcome.setHearingResult(hearingResult.getHmcValue());
            if (hearingResult == HearingResult.ADJOURNED) {
                selectedCode(bailCase, ATTENDEES_AND_DURATION_HEARING_PART_HEARD_REASON)
                    .ifPresent(hearingOutcome::setHearingResultReasonType);
            } else if (hearingResult == HearingResult.CANCELLED) {
                selectedCode(bailCase, ATTENDEES_AND_DURATION_HEARING_CANCELLATION_REASON)
                    .ifPresent(hearingOutcome::setHearingResultReasonType);
            }
        });

        return hearingOutcome;
    }

    private static List<PauseDateTime> readPauseDateTimes(BailCase bailCase, String hearingDate) {
        boolean pausedTimeRequired = bailCase.read(ATTENDEES_AND_DURATION_PAUSED_TIME_REQUIRED, YesOrNo.class)
            .map(YesOrNo.YES::equals)
            .orElse(false);
        String pauseStart = read(bailCase, ATTENDEES_AND_DURATION_PAUSE_START);
        String pauseEnd = read(bailCase, ATTENDEES_AND_DURATION_PAUSE_END);
        if (!pausedTimeRequired || pauseStart.isEmpty() || pauseEnd.isEmpty()) {
            return List.of();
        }

        return List.of(new PauseDateTime(toDateTime(hearingDate, pauseStart), toDateTime(hearingDate, pauseEnd)));
    }

    private static String read(BailCase bailCase, BailCaseFieldDefinition field) {
        return bailCase.read(field, String.class).map(String::trim).orElse("");
    }

    private static Optional<String> selectedCode(BailCase bailCase, BailCaseFieldDefinition field) {
        return bailCase.read(field, DynamicList.class)
            .map(DynamicList::getValue)
            .map(Value::getCode)
            .filter(code -> !code.isBlank());
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
