package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.*;

@Component
@Slf4j
public class RecordAdjournmentUpdateRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {

    HearingService hearingService;
    UpdateHearingPayloadService updateHearingPayloadService;

    RecordAdjournmentUpdateRequestHandler(
        HearingService hearingService,
        UpdateHearingPayloadService updateHearingPayloadService
    ) {
        this.hearingService = hearingService;
        this.updateHearingPayloadService = updateHearingPayloadService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && Objects.equals(
            Event.RECORD_ADJOURNMENT_DETAILS,
            callback.getEvent()
        );
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<DynamicList> selectedHearing = callback.getCaseDetails().getCaseData()
            .read(ADJOURNMENT_DETAILS_HEARING);
        selectedHearing.ifPresent(hearing -> {
            String hearingId = hearing.getValue().getCode();
            hearingService.updateHearing(
                    updateHearingPayloadService.createUpdateHearingPayload(
                        hearingId,
                        getHearingChannels(asylumCase),
                        getLocations(asylumCase),
                        getDuration(asylumCase),
                        //TODO: this value is to be updated with RIA-7836
                        "update Reason",
                        false,
                        updateHearingWindow(asylumCase)
                    ),
                    hearingId
            );
        });
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Optional<String> getHearingChannels(AsylumCase asylumCase) {
        Optional<DynamicList> hearingChannels = asylumCase.read(
            HEARING_CHANNEL,
            DynamicList.class
        );

        return hearingChannels.map(hearingChannel -> hearingChannel.getValue().getCode());
    }

    private Optional<String> getLocations(AsylumCase asylumCase) {
        return asylumCase.read(
            LIST_CASE_HEARING_CENTRE,
            HearingCentre.class
        ).map(HearingCentre::getEpimsId);
    }

    private Optional<Integer> getDuration(AsylumCase asylumCase) {
        return asylumCase.read(
            LIST_CASE_HEARING_LENGTH,
            String.class
        ).map(Integer::parseInt);
    }

    private HearingWindowModel updateHearingWindow(AsylumCase asylumCase) {
        String fixedDate = asylumCase.read(
                    LIST_CASE_HEARING_DATE,
                    String.class
                ).orElseThrow(() -> new IllegalStateException(LIST_CASE_HEARING_DATE + " type is not present"));

        return HearingWindowModel.builder()
                    .firstDateTimeMustBe(HearingsUtils.convertToLocalDateTimeFormat(fixedDate).toString()).build();
    }


}
