package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingChannelTypeChangingRadioButton;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingLength;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL_TYPE_CHANGING_RADIO_BUTTON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL_TYPE_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DATE_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DURATION_CHANGING_RADIO_BUTTON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DURATION_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HEARINGS;

@Component
@Slf4j
public class UpdateHearingRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {

    HearingService hearingService;


    public UpdateHearingRequestHandler(
        HearingService hearingService
    ) {
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT && Objects.equals(
            Event.UPDATE_HEARING_REQUEST,
            callback.getEvent()
        );
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<DynamicList> selectedHearing = callback.getCaseDetails().getCaseData().read(UPDATE_HEARINGS);
        selectedHearing.ifPresent(hearing -> {
            HearingGetResponse hearingResponse = hearingService.getHearing(hearing.getValue().getCode());
            asylumCase.write(
                HEARING_CHANNEL_TYPE_VALUE,
                hearingResponse.getHearingDetails().getHearingChannelDescription()
            );
            HearingChannelTypeChangingRadioButton.from(hearingResponse
                                                           .getHearingDetails()
                                                           .getHearingChannelDescription())
                .ifPresent(
                    hct -> asylumCase.write(HEARING_CHANNEL_TYPE_CHANGING_RADIO_BUTTON, hct.name()));
            asylumCase.write(
                HEARING_LOCATION_VALUE,
                hearingResponse.getHearingDetails().getHearingLocations().get(0).getLocationId()
            );
            asylumCase.write(
                HEARING_DATE_VALUE,
                getHearingDate(hearingResponse.getHearingDetails().getHearingWindow())
            );
            Optional<HearingLength> duration = HearingLength.from(hearingResponse.getHearingDetails().getDuration());
            duration.ifPresent(hearingLength -> {
                asylumCase.write(HEARING_DURATION_VALUE, hearingLength.toMeaningFullString());
                asylumCase.write(HEARING_DURATION_CHANGING_RADIO_BUTTON, String.valueOf(hearingLength.getValue()));
            });
        });

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getHearingDate(HearingWindowModel hearingWindowModel) {
        String hearingDate = "";
        if (hearingWindowModel.getDateRangeStart() != null) {
            hearingDate = HearingsUtils.convertToLocalStringFormat(HearingsUtils.convertToLocalDateFormat(
                hearingWindowModel.getDateRangeStart()));
            String dateRangeEnd;
            if (hearingWindowModel.getDateRangeEnd() != null) {
                dateRangeEnd = HearingsUtils.convertToLocalStringFormat(HearingsUtils.convertToLocalDateFormat(
                    hearingWindowModel.getDateRangeEnd()));
                hearingDate = hearingDate + " - " + dateRangeEnd;
            }
        } else if (hearingWindowModel.getFirstDateTimeMustBe() != null) {
            hearingDate = HearingsUtils.convertToLocalStringFormat(HearingsUtils.convertToLocalDateTimeFormat(
                hearingWindowModel.getFirstDateTimeMustBe()));
        }
        return hearingDate;
    }
}
