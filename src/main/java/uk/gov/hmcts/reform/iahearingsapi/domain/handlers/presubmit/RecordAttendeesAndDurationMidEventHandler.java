package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.HearingActualsGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PlannedHearingDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingActualsService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_DATE_OF_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_FINISH_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_JUDGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_METHODS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_RESULT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_STAGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_START_TIME;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAPER_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;

@Component
@Slf4j
public class RecordAttendeesAndDurationMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final String SELECT_HEARING_PAGE_ID = "selectHearingRecordAttendeesAndDuration";

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
                HearingActualsGetResponse hearingActuals = hearingActualsService.getHearingActuals(hearingId);
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
                        .map(d -> new Value(d.getPlannedStartTime(), d.getPlannedStartTime()))
                        .collect(Collectors.toList());
                DynamicList hearingDatesList = new DynamicList(new Value("", ""), hearingDateValues);
                asylumCase.write(ATTENDEES_AND_DURATION_DATE_OF_HEARING, hearingDatesList);

                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_START_TIME, "");
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_FINISH_TIME, "");
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
