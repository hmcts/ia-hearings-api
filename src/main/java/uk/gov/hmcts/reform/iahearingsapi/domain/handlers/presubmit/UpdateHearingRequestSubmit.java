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
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_UPDATE_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;


@Component
@Slf4j
public class UpdateHearingRequestSubmit implements PreSubmitCallbackHandler<AsylumCase> {

    HearingService hearingService;

    UpdateHearingPayloadService updateHearingPayloadService;

    public UpdateHearingRequestSubmit(
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
        Optional<DynamicList> selectedHearing = asylumCase.read(CHANGE_HEARINGS);
        selectedHearing.ifPresent(hearing -> {
            String hearingId = selectedHearing.get().getValue().getCode();
            boolean firstAvailableDate = false;
            String hearingDateChangeType = asylumCase.read(
                CHANGE_HEARING_DATE_TYPE,
                String.class
            ).orElse("");
            if (hearingDateChangeType.equals("FirstAvailableDate")) {
                firstAvailableDate = true;
            }

            try {
                hearingService.updateHearing(
                    updateHearingPayloadService.createUpdateHearingPayload(
                        hearingId,
                        getHearingChannels(asylumCase),
                        getLocations(asylumCase),
                        getDuration(asylumCase),
                        getReason(asylumCase),
                        firstAvailableDate,
                        updateHearingWindow(asylumCase)
                    ),
                    hearingId
                );

                clearFields(asylumCase);

            } catch (HmcException e) {
                asylumCase.write(MANUAL_UPDATE_HEARING_REQUIRED, YES);
            }
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

    private String getReason(AsylumCase asylumCase) {
        return asylumCase.read(
            CHANGE_HEARING_UPDATE_REASON,
            DynamicList.class
        ).orElseThrow(() -> new IllegalStateException(CHANGE_HEARING_UPDATE_REASON
                                                          + " type is not present")).getValue().getCode();
    }

    private HearingWindowModel updateHearingWindow(AsylumCase asylumCase) {

        String hearingDateChangeType = asylumCase.read(
            CHANGE_HEARING_DATE_TYPE,
            String.class
        ).orElse("");

        if (hearingDateChangeType.isEmpty()) {
            return null;
        }

        return switch (hearingDateChangeType) {
            case "DateToBeFixed" -> {
                String fixedDate = asylumCase.read(
                    LIST_CASE_HEARING_DATE,
                    String.class
                ).orElseThrow(() -> new IllegalStateException(LIST_CASE_HEARING_DATE + " type is not present"));

                yield HearingWindowModel.builder()
                    .firstDateTimeMustBe(HearingsUtils.convertToLocalDateTimeFormat(fixedDate).toString()).build();
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
