package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingLength;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;

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

        Optional<DynamicList> selectedHearing = callback.getCaseDetails().getCaseData().read(CHANGE_HEARINGS);
        selectedHearing.ifPresent(hearing -> {
            HearingGetResponse hearingResponse = hearingService.getHearing(hearing.getValue().getCode());
            if (hearingResponse.getHearingDetails().getHearingChannels() != null
                && !hearingResponse.getHearingDetails().getHearingChannels().isEmpty()) {
                asylumCase.write(
                    CHANGE_HEARING_TYPE_VALUE,
                    hearingResponse.getHearingDetails().getHearingChannelDescription()
                );
                List<Value> hearingChannels = Arrays.stream(HearingChannel
                                                                .values())
                    .map(hearingChannel -> new Value(hearingChannel.name(), hearingChannel.getLabel()))
                    .toList();
                asylumCase.write(HEARING_CHANNEL, new DynamicList(
                    new Value(
                        hearingResponse.getHearingDetails().getHearingChannels().get(0),
                        hearingResponse.getHearingDetails().getHearingChannelDescription()
                    ),
                    hearingChannels
                ));
            }

            if (hearingResponse.getHearingDetails().getHearingLocations() != null
                && !hearingResponse.getHearingDetails().getHearingLocations().isEmpty()) {
                asylumCase.write(
                    CHANGE_HEARING_LOCATION_VALUE,
                    hearingResponse.getHearingDetails().getHearingLocations().get(0).getLocationId()
                );
            }
            if (hearingResponse.getHearingDetails().getHearingWindow() != null) {
                asylumCase.write(
                    CHANGE_HEARING_DATE_VALUE,
                    getHearingDate(hearingResponse.getHearingDetails().getHearingWindow())
                );
            }
            if (hearingResponse.getHearingDetails().getDuration() != null) {
                Optional<HearingLength> duration = HearingLength.from(hearingResponse
                                                                          .getHearingDetails()
                                                                          .getDuration());
                duration.ifPresent(hearingLength -> {
                    asylumCase.write(CHANGE_HEARING_DURATION_VALUE, hearingLength.convertToHourMinuteString());
                    asylumCase.write(LIST_CASE_HEARING_LENGTH, String.valueOf(hearingLength.getValue()));
                });
            }

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
            hearingDate = HearingsUtils.convertToLocalStringFormat(LocalDateTime.parse(
                hearingWindowModel.getFirstDateTimeMustBe()));
        }
        return hearingDate;
    }
}
