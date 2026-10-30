package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualDayParty;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.ActualIndividualDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingOutcome;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingResult;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PausedDateTime;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingActualsRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
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

        String hearingDate = "10-10-2026";
        Optional<String> hearingIdOpt = asylumCase.read(ATTENDEES_AND_DURATION_HEARING_ID);
        if (hearingIdOpt.isPresent()) {
            log.info("------hearingIdOpt: {}", hearingIdOpt);
            asylumCase.write(ATTENDEES_AND_DURATION_HEARING_START_TIME, "");
            asylumCase.write(ATTENDEES_AND_DURATION_HEARING_FINISH_TIME, "");

            HearingOutcome hearingOutcome = new HearingOutcome();
            hearingOutcome.setHearingType(HearingType.SUBSTANTIVE);
            hearingOutcome.setHearingFinalFlag("No");
            hearingOutcome.setHearingResult(HearingResult.HEARD);
            hearingOutcome.setHearingResultReasonType("Reason");
            hearingOutcome.setHearingResultDate(hearingDate);

            ActualHearingDay actualHearingDay = new ActualHearingDay();
            actualHearingDay.setHearingDate(hearingDate);
            actualHearingDay.setHearingStartTime("10:00");
            actualHearingDay.setHearingEndTime("12:00");

            PausedDateTime pausedDateTime = new PausedDateTime();
            pausedDateTime.setPauseStartTime("10:10");
            pausedDateTime.setPauseEndTime("10:20");
            List<PausedDateTime> pausedDateTimes = new ArrayList<>();
            pausedDateTimes.add(pausedDateTime);
            actualHearingDay.setPausedDateTimes(pausedDateTimes);

            ActualDayParty actualDayParty = new ActualDayParty();
            actualDayParty.setActualPartyId("123");
            actualDayParty.setPartyRole("Role");
            ActualIndividualDetails actualIndividualDetails = new ActualIndividualDetails();
            actualIndividualDetails.setFirstName("John");
            actualIndividualDetails.setLastName("Smith");
            actualDayParty.setIndividualDetails(actualIndividualDetails);
            actualDayParty.setPartyChannelSubType("INTER");
            actualDayParty.setDidNotAttendFlag("No");
            actualDayParty.setRepresentedParty("RepresentedParty");
            List<ActualDayParty> actualDayParties = new ArrayList<>();
            actualDayParties.add(actualDayParty);
            actualHearingDay.setActualDayParties(actualDayParties);

            actualHearingDay.setNotRequired("No");
            List<ActualHearingDay> actualHearingDays = new ArrayList<>();
            actualHearingDays.add(actualHearingDay);

            UpdateHearingActualsRequest request = new UpdateHearingActualsRequest();
            request.setHearingOutcome(hearingOutcome);
            request.setActualHearingDays(actualHearingDays);

            String hearingId = hearingIdOpt.get();
            hearingActualsService.updateHearingActuals(request, hearingId);

            log.info("Updated hearing actuals {} for hearing id {}", request, hearingId);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
