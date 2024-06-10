package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_LOCATION_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE_YES_NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_UPDATE_REASON;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_VENUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_DATE_1;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.getValueByEpimsId;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus.AWAITING_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus.CASE_CREATED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit.UpdateHearingRequestHandler.setHearingDateDetailsFromRequestedHearing;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.UpdateHearingPayloadService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;


@Component
@Slf4j
public class UpdateHearingRequestSubmit implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String YES = "Yes";
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
                        asylumCase,
                        hearingId,
                        getReason(asylumCase),
                        firstAvailableDate,
                        updateHearingWindow(asylumCase),
                        Event.UPDATE_HEARING_REQUEST,
                        callback.getCaseDetails().getId()
                    ),
                    hearingId
                );

                HearingGetResponse updatedHearing = hearingService.getHearing(hearingId);

                if (isHearingYetToBeListed(updatedHearing)) {
                    rewriteRequestedFields(asylumCase);
                }

                clearFields(asylumCase);

            } catch (HmcException e) {
                asylumCase.write(MANUAL_UPDATE_HEARING_REQUIRED, YES);
            }
        });


        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void rewriteRequestedFields(AsylumCase asylumCase) {
        if (isHearingTypeUpdated(asylumCase)) {
            rewriteHearingChannel(asylumCase);
        }
        if (isLocationUpdated(asylumCase)) {
            rewriteLocation(asylumCase);
        }
        if (isDurationUpdated(asylumCase)) {
            rewriteDuration(asylumCase);
        }
        if (isDateTimeUpdated(asylumCase)) {
            rewriteDateTime(asylumCase);
        }
    }

    private void rewriteHearingChannel(AsylumCase asylumCase) {
        asylumCase.read(REQUEST_HEARING_CHANNEL, DynamicList.class)
            .ifPresentOrElse(hearingChannel -> {
                asylumCase.write(CHANGE_HEARING_TYPE, hearingChannel.getValue().getLabel());
                asylumCase.write(HEARING_CHANNEL, hearingChannel);
                },
                             throwRequiredFieldMissingError("Request Hearing Channel missing")
            );
    }

    private void rewriteLocation(AsylumCase asylumCase) {
        asylumCase.read(HEARING_LOCATION, DynamicList.class)
            .map(dynamicList -> getValueByEpimsId(dynamicList.getValue().getCode()))
            .ifPresentOrElse(
                hearingVenueCodeName -> asylumCase.write(CHANGE_HEARING_VENUE, hearingVenueCodeName),
                throwRequiredFieldMissingError("Hearing Location missing or unrecognized epims id"));
    }

    private void rewriteDuration(AsylumCase asylumCase) {
        asylumCase.read(REQUEST_HEARING_LENGTH, String.class)
                .ifPresentOrElse(duration -> {
                    asylumCase.write(CHANGE_HEARING_DURATION, duration);
                    asylumCase.write(LISTING_LENGTH, new HoursMinutes(Integer.parseInt(duration)));
                },
                                 throwRequiredFieldMissingError("Request Hearing Length missing"));
    }

    private void rewriteDateTime(AsylumCase asylumCase) {
        HearingWindowModel updatedHearingWindow = updateHearingWindow(asylumCase);
        if (updatedHearingWindow != null) {
            setHearingDateDetailsFromRequestedHearing(asylumCase, updatedHearingWindow);
        } else {
            asylumCase.write(CHANGE_HEARING_DATE, "First available date");
        }
    }

    private boolean isLocationUpdated(AsylumCase asylumCase) {
        return asylumCase.read(CHANGE_HEARING_LOCATION_YES_NO, String.class)
            .map(this::isYes).orElse(false);
    }

    private boolean isDurationUpdated(AsylumCase asylumCase) {
        return asylumCase.read(CHANGE_HEARING_DURATION_YES_NO, String.class)
            .map(this::isYes).orElse(false);
    }

    private boolean isDateTimeUpdated(AsylumCase asylumCase) {
        return asylumCase.read(CHANGE_HEARING_DATE_YES_NO, String.class)
            .map(this::isYes).orElse(false);
    }

    private boolean isHearingTypeUpdated(AsylumCase asylumCase) {
        return asylumCase.read(CHANGE_HEARING_TYPE_YES_NO, String.class)
            .map(this::isYes).orElse(false);
    }

    private boolean isYes(String yesOrNo) {
        return StringUtils.equalsIgnoreCase(yesOrNo, "Yes");
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
                    REQUEST_HEARING_DATE_1,
                    String.class
                ).orElseThrow(() -> new IllegalStateException(REQUEST_HEARING_DATE_1 + " type is not present"));

                yield HearingWindowModel.builder()
                    .firstDateTimeMustBe(HearingsUtils.convertToLocalDateTimeFormat(fixedDate)
                                             .withHour(16).toString()).build();
            }
            case "ChooseADateRange" -> {
                HearingWindowModel window = HearingWindowModel.builder().build();

                asylumCase.read(CHANGE_HEARING_DATE_RANGE_EARLIEST, String.class)
                    .ifPresent(window::setDateRangeStart);
                asylumCase.read(CHANGE_HEARING_DATE_RANGE_LATEST, String.class)
                    .ifPresent(window::setDateRangeEnd);

                yield window;
            }
            default -> null;
        };
    }


    private void clearFields(AsylumCase asylumCase) {
        asylumCase.clear(CHANGE_HEARINGS);
        asylumCase.write(CHANGE_HEARING_TYPE_YES_NO, "no");
        asylumCase.write(CHANGE_HEARING_LOCATION_YES_NO, "no");
        asylumCase.write(CHANGE_HEARING_DURATION_YES_NO, "no");
        asylumCase.write(CHANGE_HEARING_DATE_YES_NO, "no");
        asylumCase.clear(CHANGE_HEARING_UPDATE_REASON);
        asylumCase.clear(CHANGE_HEARING_DATE_TYPE);
        asylumCase.clear(CHANGE_HEARING_DATE_RANGE_EARLIEST);
        asylumCase.clear(CHANGE_HEARING_DATE_RANGE_LATEST);
    }

    private boolean isHearingYetToBeListed(HearingGetResponse hearing) {
        ListAssistCaseStatus laCaseStatus = hearing.getHearingResponse().getLaCaseStatus();
        return laCaseStatus == null || Set.of(CASE_CREATED, AWAITING_LISTING).contains(laCaseStatus);
    }

    private Runnable throwRequiredFieldMissingError(String message) {
        return () -> {
            throw new RequiredFieldMissingException("Hearing Location missing or unrecognized epims id");
        };
    }
}
