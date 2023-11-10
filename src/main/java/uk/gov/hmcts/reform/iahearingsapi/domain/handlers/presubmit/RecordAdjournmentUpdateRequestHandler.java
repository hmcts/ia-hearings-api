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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CANCELLATION_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;

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

        boolean relistCaseImmediately = asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
            .map(relist -> YES == relist)
            .orElseThrow(() -> new IllegalStateException("Response to relist case immediately is not present"));

        DynamicList hearingList = asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("Adjournment details hearing is not present"));

        String hearingId = hearingList.getValue().getCode();

        if (relistCaseImmediately) {
            updateHearing(asylumCase, hearingId);
        } else {
            deleteHearing(asylumCase, hearingId);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void deleteHearing(AsylumCase asylumCase, String hearingId) {
        String cancellationReason = asylumCase.read(HEARING_CANCELLATION_REASON, String.class)
            .orElseThrow(() -> new IllegalStateException("Hearing cancellation reason is not present"));

        YesOrNo manualCancelHearingRequired = NO;

        try {
            hearingService.deleteHearing(Long.valueOf(hearingId), cancellationReason);
        } catch (HmcException e) {
            manualCancelHearingRequired = YES;
        }

        asylumCase.write(MANUAL_CANCEL_HEARINGS_REQUIRED, manualCancelHearingRequired);
    }

    private void updateHearing(AsylumCase asylumCase, String hearingId) {
        YesOrNo updateRequestSuccess = YES;
        try {
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
        } catch (HmcException ex) {
            log.error("Error updating HMC hearing details. " + ex);
            updateRequestSuccess = NO;
        }
        asylumCase.write(UPDATE_HMC_REQUEST_SUCCESS, updateRequestSuccess);
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
