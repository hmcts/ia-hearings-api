package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ADJOURNMENT_WHEN;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_CANCEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_REASON_TO_UPDATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_FIXED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HMC_REQUEST_SUCCESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationBasedFeatureToggler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecordAdjournmentUpdateRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {
    public static final String NEXT_HEARING_DATE_FIRST_AVAILABLE_DATE = "FirstAvailableDate";
    public static final String NEXT_HEARING_DATE_DATE_TO_BE_FIXED = "DateToBeFixed";
    public static final String NEXT_HEARING_DATE_CHOOSE_DATE_RANGE = "ChooseADateRange";

    private final HearingService hearingService;
    private final UpdateHearingPayloadService updateHearingPayloadService;
    private final LocationBasedFeatureToggler locationBasedFeatureToggler;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && RECORD_ADJOURNMENT_DETAILS == callback.getEvent();
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (shouldAutoRequestHearing(asylumCase)) {
            hearingService.createHearingWithPayload(callback);
        } else if (shouldUpdateHearing(asylumCase)) {
            updateHearing(asylumCase, getHearingId(asylumCase));
        } else if (shouldDeleteHearing(asylumCase)) {
            deleteHearing(asylumCase, getHearingId(asylumCase));
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getHearingId(AsylumCase asylumCase) {
        DynamicList hearingList = asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("Adjournment details hearing is not present"));

        return hearingList.getValue().getCode();
    }

    private boolean relistCaseImmediately(AsylumCase asylumCase) {
        return asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
            .map(relist -> YES == relist)
            .orElseThrow(() -> new IllegalStateException("Response to relist case immediately is not present"));
    }

    private HearingAdjournmentDay getHearingAdjournmentDay(AsylumCase asylumCase) {
        return asylumCase
            .read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class)
            .orElseThrow(() -> new IllegalStateException("'Hearing adjournment when' is not present"));
    }

    private boolean shouldUpdateHearing(AsylumCase asylumCase) {
        return relistCaseImmediately(asylumCase)
               && (getHearingAdjournmentDay(asylumCase) == BEFORE_HEARING_DATE);
    }

    private boolean shouldDeleteHearing(AsylumCase asylumCase) {
        return !relistCaseImmediately(asylumCase)
               && (getHearingAdjournmentDay(asylumCase) == BEFORE_HEARING_DATE);
    }

    private boolean shouldAutoRequestHearing(AsylumCase asylumCase) {
        boolean adjournedOnHearingDay = getHearingAdjournmentDay(asylumCase) == ON_HEARING_DATE;
        boolean autoRequestHearingEnabled =
            locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == YES;

        return autoRequestHearingEnabled
               && relistCaseImmediately(asylumCase)
               && adjournedOnHearingDay;
    }

    private void deleteHearing(AsylumCase asylumCase, String hearingId) {
        DynamicList cancellationReason = asylumCase.read(HEARING_REASON_TO_CANCEL, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("Hearing cancellation reason is not present"));

        YesOrNo manualCancelHearingRequired = NO;

        try {
            hearingService.deleteHearing(Long.valueOf(hearingId), cancellationReason.getValue().getCode());
        } catch (HmcException e) {
            manualCancelHearingRequired = YES;
        }

        asylumCase.write(MANUAL_CANCEL_HEARINGS_REQUIRED, manualCancelHearingRequired);
    }

    private void updateHearing(AsylumCase asylumCase, String hearingId) {
        YesOrNo updateRequestSuccess = YES;
        DynamicList cancellationReason = asylumCase.read(HEARING_REASON_TO_UPDATE, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("Hearing relisted cancellation reason is not present"));
        String nextHearingDate = asylumCase.read(NEXT_HEARING_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException(NEXT_HEARING_DATE + "  is not present"));

        try {

            hearingService.updateHearing(
                updateHearingPayloadService.createUpdateHearingPayload(
                    asylumCase,
                    hearingId,
                    cancellationReason.getValue().getCode(),
                    nextHearingDate.equals(NEXT_HEARING_DATE_FIRST_AVAILABLE_DATE),
                    updateHearingWindow(asylumCase),
                    true
                ),
                hearingId
            );
        } catch (HmcException ex) {
            log.error("Error updating HMC hearing details. " + ex);
            updateRequestSuccess = NO;
        }
        asylumCase.write(UPDATE_HMC_REQUEST_SUCCESS, updateRequestSuccess);
        asylumCase.write(MANUAL_CANCEL_HEARINGS_REQUIRED, NO);
    }


    private HearingWindowModel updateHearingWindow(AsylumCase asylumCase) {
        String nextHearingDate = asylumCase.read(NEXT_HEARING_DATE, String.class)
            .orElseThrow(() -> new IllegalStateException(NEXT_HEARING_DATE + "  is not present"));
        return switch (nextHearingDate) {
            case NEXT_HEARING_DATE_DATE_TO_BE_FIXED -> {
                String dateFixed = asylumCase.read(NEXT_HEARING_DATE_FIXED, String.class)
                    .orElseThrow(() -> new IllegalStateException(NEXT_HEARING_DATE_FIXED + "  is not present"));
                yield HearingWindowModel.builder()
                    .firstDateTimeMustBe(HearingsUtils.convertToLocalDateFormat(dateFixed).toString())
                    .build();
            }
            case NEXT_HEARING_DATE_CHOOSE_DATE_RANGE -> {
                HearingWindowModel hearingWindowModel = HearingWindowModel.builder().build();
                asylumCase.read(NEXT_HEARING_DATE_RANGE_EARLIEST, String.class)
                    .ifPresent(date ->
                                   hearingWindowModel.setDateRangeStart(
                                       HearingsUtils.convertToLocalDateFormat(date).toString()));

                asylumCase.read(NEXT_HEARING_DATE_RANGE_LATEST, String.class)
                    .ifPresent(date ->
                                   hearingWindowModel.setDateRangeEnd(
                                       HearingsUtils.convertToLocalDateFormat(date).toString()));
                yield  hearingWindowModel;
            }
            default -> null;
        };
    }

}
