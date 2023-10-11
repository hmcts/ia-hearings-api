package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
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
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_UPDATE_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;


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
        Optional<DynamicList> selectedHearing = callback.getCaseDetails().getCaseData().read(CHANGE_HEARINGS);
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
        Optional<DynamicList> hearingChannels = asylumCase.read(
            HEARING_CHANNEL,
            DynamicList.class
        );

        return hearingChannels.map(hearingChannel -> List.of(hearingChannel.getValue().getCode())
        ).orElseGet(() -> persistedHearing.getHearingDetails().getHearingChannels());
    }

    private List<HearingLocationModel> getLocations(AsylumCase asylumCase, HearingGetResponse persistedHearing) {
        Optional<HearingCentre> locationUpdate =
            asylumCase.read(
                LIST_CASE_HEARING_CENTRE,
                HearingCentre.class
            );

        return locationUpdate.map(location
                                      -> List.of(HearingLocationModel.builder()
                                                     .locationId(location.getEpimsId())
                                                     .locationType(persistedHearing
                                                                       .getHearingDetails()
                                                                       .getHearingLocations().get(0)
                                                                       .getLocationType()).build()))
            .orElseGet(() -> persistedHearing.getHearingDetails().getHearingLocations());

    }

    private Integer getDuration(AsylumCase asylumCase, HearingGetResponse persistedHearing) {
        Optional<String> durationUpdate =
            asylumCase.read(
                LIST_CASE_HEARING_LENGTH,
                String.class
            );

        return durationUpdate.map(Integer::parseInt)
            .orElseGet(() -> persistedHearing.getHearingDetails().getDuration());

    }

    private List<String> getReasons(AsylumCase asylumCase) {
        Value reasonUpdate =
            asylumCase.read(
                CHANGE_HEARING_UPDATE_REASON,
                DynamicList.class
            ).orElseThrow(() -> new IllegalStateException(CHANGE_HEARING_UPDATE_REASON
                                                              + " type is not present")).getValue();

        return List.of(reasonUpdate.getCode());

    }

    private HearingWindowModel updateHearingWindow(AsylumCase asylumCase, HearingGetResponse persistedHearing) {

        String hearingDateChangeType = asylumCase.read(
            CHANGE_HEARING_DATE_TYPE,
            String.class
        ).orElse("");
        if (hearingDateChangeType.isEmpty()) {
            return persistedHearing.getHearingDetails().getHearingWindow();
        }

        return switch (hearingDateChangeType) {
            case "DateToBeFixed" -> {
                String fixedDate = asylumCase.read(
                    LIST_CASE_HEARING_DATE,
                    String.class
                ).orElseThrow(() -> new IllegalStateException(LIST_CASE_HEARING_DATE + " type is not present"));

                yield HearingWindowModel.builder()
                    .firstDateTimeMustBe(HearingsUtils.convertToLocalDateFormat(fixedDate).toString()).build();
            }
            case "ChooseADateRange" -> {
                String earliestDate = asylumCase.read(
                    CHANGE_HEARING_DATE_RANGE_EARLIEST,
                    String.class
                ).orElseThrow(() -> new IllegalStateException(
                    CHANGE_HEARING_DATE_RANGE_EARLIEST + " type is not present"));

                String latestDate = asylumCase.read(
                    CHANGE_HEARING_DATE_RANGE_LATEST,
                    String.class
                ).orElseThrow(() -> new IllegalStateException(
                    CHANGE_HEARING_DATE_RANGE_LATEST + " type is not present"));
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
        asylumCase.clear(CHANGE_HEARINGS);
        asylumCase.write(CHANGE_HEARING_TYPE_YES_NO, "no");
        asylumCase.clear(HEARING_CHANNEL);
        asylumCase.write(CHANGE_HEARING_LOCATION_YES_NO, "no");
        asylumCase.clear(LIST_CASE_HEARING_CENTRE);
        asylumCase.write(CHANGE_HEARING_DURATION_YES_NO, "no");
        asylumCase.clear(LIST_CASE_HEARING_LENGTH);
        asylumCase.write(CHANGE_HEARING_DATE_YES_NO, "no");
        asylumCase.clear(CHANGE_HEARING_UPDATE_REASON);
        asylumCase.clear(CHANGE_HEARING_DATE_TYPE);
        asylumCase.clear(LIST_CASE_HEARING_DATE);
        asylumCase.clear(CHANGE_HEARING_DATE_RANGE_EARLIEST);
        asylumCase.clear(CHANGE_HEARING_DATE_RANGE_LATEST);
    }
}
