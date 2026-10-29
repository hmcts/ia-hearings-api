package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_HEARING_METHODS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ATTENDEES_AND_DURATION_PAPER_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ATTENDEES_AND_DURATION;

@Component
@Slf4j
public class RecordAttendeesAndDurationMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private static final String SELECT_ATTENDEES_AND_DURATION_HEARING_LIST_PAGE_ID =
        "selectHearingRecordAttendeesAndDuration";

    private final HearingService hearingService;

    public RecordAttendeesAndDurationMidEventHandler(
        HearingService hearingService
    ) {
        this.hearingService = hearingService;
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

        if (Objects.equals(pageId, SELECT_ATTENDEES_AND_DURATION_HEARING_LIST_PAGE_ID)) {
            Optional<DynamicList> selectedHearingOptional =
                asylumCase.read(ATTENDEES_AND_DURATION_HEARING, DynamicList.class);
            log.info("-----selectedHearingOptional {}", selectedHearingOptional);
            if (selectedHearingOptional.isPresent()) {
                DynamicList selectedHearing = selectedHearingOptional.get();
                String hearingId = selectedHearing.getValue().getCode();
                log.info("-----selected hearingId {}", hearingId);
                HearingDetails hearingDetails = hearingService.getHearing(hearingId).getHearingDetails();
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_ID, hearingId);
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_DATE, hearingId);
                asylumCase.write(ATTENDEES_AND_DURATION_PAPER_HEARING, hearingId);
                asylumCase.write(ATTENDEES_AND_DURATION_HEARING_METHODS, hearingId);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
