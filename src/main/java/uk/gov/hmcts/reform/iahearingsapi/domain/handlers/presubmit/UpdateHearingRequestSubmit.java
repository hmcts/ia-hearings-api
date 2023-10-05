package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL_TYPE_CHANGING_RADIO_BUTTON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL_TYPE_RADIO_BUTTON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DATE_CHANGE_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHOOSE_A_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHOOSE_A_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DATE_RADIO_BUTTON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.DATE_TO_BE_FIXED_VALUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DURATION_CHANGING_RADIO_BUTTON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DURATION_RADIO_BUTTON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION_CHANGE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION_RADIO_BUTTON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_UPDATE_REASON_LIST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.UPDATE_HEARINGS;


@Component
@Slf4j
public class UpdateHearingRequestSubmit implements PreSubmitCallbackHandler<AsylumCase> {

    HearingService hearingService;


    public UpdateHearingRequestSubmit(
        HearingService hearingService
    ) {
        this.hearingService = hearingService;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && Objects.equals(
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
            String hearingId = selectedHearing.get().getValue().getCode();
            HearingGetResponse persistedHearing = hearingService.getHearing(hearingId);

            HearingDetails hearingDetails = HearingDetails.builder()
                .hearingChannels(getHearingChannels(asylumCase, persistedHearing))
                .hearingLocations(getLocations(asylumCase, persistedHearing))
                .duration(getDuration(asylumCase, persistedHearing))
                .amendReasonCodes(getReasons(asylumCase))
                .hearingWindow(updateHearingWindow(asylumCase, persistedHearing))
                .build();


            UpdateHearingRequest updateHearingRequest = UpdateHearingRequest.builder()
                .requestDetails(persistedHearing.getRequestDetails())
                .caseDetails(persistedHearing.getCaseDetails())
                .hearingDetails(buildHearingDetails(persistedHearing.getHearingDetails(), hearingDetails))
                .partyDetails(persistedHearing.getPartyDetails())
                .build();

            HearingGetResponse hearingUpdated = hearingService.updateHearing(
                updateHearingRequest,
                hearingId
            );

            if (hearingUpdated != null) {
                clearFields(asylumCase);
            }
        });


        return new PreSubmitCallbackResponse<>(asylumCase);
    }


    private List<String> getHearingChannels(AsylumCase asylumCase, HearingGetResponse persistedHearing) {
        Optional<String> hearingChannels = asylumCase.read(
            HEARING_CHANNEL_TYPE_CHANGING_RADIO_BUTTON,
            String.class
        );

        return hearingChannels.map(List::of).orElseGet(() -> persistedHearing.getHearingDetails().getHearingChannels());
    }

    private List<HearingLocationModel> getLocations(AsylumCase asylumCase, HearingGetResponse persistedHearing) {
        Optional<DynamicList> locationUpdate =
            asylumCase.read(
                HEARING_LOCATION_CHANGE,
                DynamicList.class
            );

        return locationUpdate.map(dynamicList -> List.of(HearingLocationModel.builder()
                                                             .locationId(dynamicList.getValue().getCode())
                                                             .locationType(persistedHearing.getHearingDetails().getHearingLocations().get(
                                                                 0).getLocationType()).build()))
            .orElseGet(() -> persistedHearing.getHearingDetails().getHearingLocations());

    }

    private Integer getDuration(AsylumCase asylumCase, HearingGetResponse persistedHearing) {
        Optional<DynamicList> durationUpdate =
            asylumCase.read(
                HEARING_DURATION_CHANGING_RADIO_BUTTON,
                DynamicList.class
            );

        return durationUpdate.map(dynamicList -> Integer.parseInt(dynamicList.getValue().getCode()))
            .orElseGet(() -> persistedHearing.getHearingDetails().getDuration());

    }

    private List<String> getReasons(AsylumCase asylumCase) {
        Value reasonUpdate =
            asylumCase.read(
                HEARING_UPDATE_REASON_LIST,
                DynamicList.class
            ).orElseThrow(() -> new IllegalStateException(HEARING_UPDATE_REASON_LIST + " type is not present")).getValue();

        return List.of(reasonUpdate.getCode());

    }

    private HearingWindowModel updateHearingWindow(AsylumCase asylumCase, HearingGetResponse persistedHearing) {

        String hearingDateChangeType = asylumCase.read(
            HEARING_DATE_CHANGE_DATE,
            String.class
        ).orElse("");
        if (hearingDateChangeType.isEmpty()) {
            return persistedHearing.getHearingDetails().getHearingWindow();
        }

        return switch (hearingDateChangeType) {
            case "DateToBeFixed" -> {
                String fixedDate = asylumCase.read(
                    DATE_TO_BE_FIXED_VALUE,
                    String.class
                ).orElseThrow(() -> new IllegalStateException(DATE_TO_BE_FIXED_VALUE + " type is not present"));

                yield HearingWindowModel.builder().
                    firstDateTimeMustBe(HearingsUtils.convertToLocalDateFormat(fixedDate).toString()).build();
            }
            case "ChooseADateRange" -> {
                String earliestDate = asylumCase.read(
                    CHOOSE_A_DATE_RANGE_EARLIEST,
                    String.class
                ).orElseThrow(() -> new IllegalStateException(
                    CHOOSE_A_DATE_RANGE_EARLIEST + " type is not present"));

                String latestDate = asylumCase.read(
                    CHOOSE_A_DATE_RANGE_LATEST,
                    String.class
                ).orElseThrow(() -> new IllegalStateException(
                    CHOOSE_A_DATE_RANGE_LATEST + " type is not present"));
                yield HearingWindowModel.builder()
                    .dateRangeStart(earliestDate)
                    .dateRangeEnd(latestDate)
                    .build();

            }
            default -> null;
        };
    }

    private HearingDetails buildHearingDetails(HearingDetails hearingDetails, HearingDetails updatedHearingsDetails) {
        hearingDetails.setHearingChannels(updatedHearingsDetails.getHearingChannels());
        hearingDetails.setHearingLocations(updatedHearingsDetails.getHearingLocations());
        hearingDetails.setDuration(updatedHearingsDetails.getDuration());
        hearingDetails.setAmendReasonCodes(updatedHearingsDetails.getAmendReasonCodes());
        hearingDetails.setHearingWindow(updatedHearingsDetails.getHearingWindow());
        return hearingDetails;
    }

    private void clearFields(AsylumCase asylumCase) {
        asylumCase.clear(UPDATE_HEARINGS);
        asylumCase.write(HEARING_CHANNEL_TYPE_RADIO_BUTTON, "no");
        asylumCase.clear(HEARING_CHANNEL_TYPE_CHANGING_RADIO_BUTTON);
        asylumCase.write(HEARING_LOCATION_RADIO_BUTTON, "no");
        asylumCase.clear(HEARING_LOCATION_CHANGE);
        asylumCase.write(HEARING_DURATION_RADIO_BUTTON, "no");
        asylumCase.clear(HEARING_DURATION_CHANGING_RADIO_BUTTON);
        asylumCase.write(HEARING_DATE_RADIO_BUTTON, "no");
        asylumCase.clear(HEARING_UPDATE_REASON_LIST);
        asylumCase.clear(HEARING_DATE_CHANGE_DATE);
        asylumCase.clear(DATE_TO_BE_FIXED_VALUE);
        asylumCase.clear(CHOOSE_A_DATE_RANGE_EARLIEST);
        asylumCase.clear(CHOOSE_A_DATE_RANGE_LATEST);
    }
}
