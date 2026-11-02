package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActuals;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingOutcome;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingResult;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PausedDateTime;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_START_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecordAttendeesAndDurationRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {
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

            String hearingDate = asylumCase.read(ATTENDEES_AND_DURATION_HEARING_DATE, String.class).orElse("");
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
                log.info("hearingResultStr is {}", HearingResult.from(hearingResultStr).get());
                hearingOutcome.setHearingResult(HearingResult.from(hearingResultStr).get().getHmcValue());
            } else {
                log.info("hearingResultStr is {}", "None");
                hearingOutcome.setHearingResult("None");
            }
            HearingActuals hearingActuals = new HearingActuals();
            hearingActuals.setHearingOutcome(hearingOutcome);

            ActualHearingDay actualHearingDay = new ActualHearingDay();
            actualHearingDay.setHearingDate(hearingDate);
            String hearingStartTime =
                asylumCase.read(ATTENDEES_AND_DURATION_HEARING_START_TIME, String.class).orElse("");
            actualHearingDay.setHearingStartTime(hearingStartTime);
            String hearingEndTime =
                asylumCase.read(ATTENDEES_AND_DURATION_HEARING_FINISH_TIME, String.class).orElse("");
            actualHearingDay.setHearingEndTime(hearingEndTime);

            PausedDateTime pausedDateTime = new PausedDateTime();
            pausedDateTime.setPauseStartTime("10:10");
            pausedDateTime.setPauseEndTime("10:20");
            List<PausedDateTime> pausedDateTimes = new ArrayList<>();
            pausedDateTimes.add(pausedDateTime);
            actualHearingDay.setPausedDateTimes(pausedDateTimes);

            actualHearingDay.setNotRequired("No");
            Set<ActualHearingDay> actualHearingDays = new HashSet<>();
            actualHearingDays.add(actualHearingDay);

            String hearingId = hearingIdOpt.get();
            HearingActualsData hearingActualsData = hearingActualsService.getHearingActuals(hearingId);
            log.info("hearingActualsData {}", hearingActualsData);
            hearingActuals.setActualHearingDays(actualHearingDays);
            hearingActualsData.setHearingActuals(hearingActuals);

            log.info("Updating hearing actuals {} for hearing id {}", hearingActualsData, hearingId);
            hearingActualsService.updateHearingActuals(hearingActualsData, hearingId);
            log.info("Updated hearing actuals {} for hearing id {}", hearingActualsData, hearingId);
            hearingActualsService.completeHearingActuals(hearingId);
            log.info("Completed hearing actuals for hearing id {}", hearingId);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
