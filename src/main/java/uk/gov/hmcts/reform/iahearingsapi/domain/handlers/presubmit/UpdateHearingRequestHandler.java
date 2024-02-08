package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARINGS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_EARLIEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DATE_RANGE_LATEST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CHANGE_HEARING_VENUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingLength;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateHearingRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingService hearingService;
    private final LocationRefDataService locationRefDataService;

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

        Optional<DynamicList> selectedHearingOptional = asylumCase.read(CHANGE_HEARINGS, DynamicList.class);
        if (selectedHearingOptional.isPresent()) {
            DynamicList selectedHearing = selectedHearingOptional.get();
            String hearingId = selectedHearing.getValue().getCode();
            HearingDetails hearingDetails = hearingService
                .getHearing(hearingId).getHearingDetails();
            setHearingChannelDetails(asylumCase, hearingDetails);
            setHearingLocationDetails(asylumCase, hearingDetails);
            setHearingDateDetails(asylumCase, hearingDetails.getHearingWindow());
            setHearingDurationDetails(asylumCase, hearingDetails);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setHearingChannelDetails(AsylumCase asylumCase, HearingDetails hearingDetails) {
        asylumCase.read(HEARING_CHANNEL, DynamicList.class).ifPresentOrElse(hearingChannel -> {
            asylumCase.write(CHANGE_HEARING_TYPE, hearingChannel.getValue().getLabel());
            asylumCase.write(REQUEST_HEARING_CHANNEL, hearingChannel);
        }, () -> {
            List<String> hearingChannels = hearingDetails.getHearingChannels();
            if (!(hearingChannels == null || hearingChannels.isEmpty())) {
                asylumCase.write(
                    CHANGE_HEARING_TYPE,
                    hearingDetails.getHearingChannelDescription()
                );
                asylumCase.write(REQUEST_HEARING_CHANNEL, new DynamicList(
                    new Value(
                        hearingDetails.getHearingChannels().get(0),
                        hearingDetails.getHearingChannelDescription()
                    ),
                    Arrays.stream(HearingChannel.values())
                        .map(hearingChannel -> new Value(hearingChannel.name(), hearingChannel.getLabel()))
                        .toList()
                ));
            }
        });
    }

    private void setHearingLocationDetails(AsylumCase asylumCase, HearingDetails hearingDetails) {
        DynamicList hearingLocation = asylumCase.read(HEARING_LOCATION, DynamicList.class)
            .orElseGet(locationRefDataService::getHearingLocationsDynamicList);
        HearingCentre listCaseHearingCentre = asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .orElse(null);
        if (listCaseHearingCentre != null) {
            asylumCase.write(CHANGE_HEARING_VENUE, listCaseHearingCentre.getValue());
            if (!(listCaseHearingCentre == REMOTE_HEARING || listCaseHearingCentre == DECISION_WITHOUT_HEARING)) {
                Value hearingVenueValue = getHearingVenueValue(hearingLocation, listCaseHearingCentre.getEpimsId());
                hearingLocation.setValue(hearingVenueValue);
                asylumCase.write(HEARING_LOCATION, hearingLocation);
            }
        } else {
            List<HearingLocationModel> hearingLocations = hearingDetails.getHearingLocations();
            if (hearingLocations == null || hearingLocations.isEmpty()) {
                asylumCase.write(
                    CHANGE_HEARING_VENUE,
                    HearingCentre.getHearingCentreByEpimsId(hearingLocation.getValue().getCode()).getValue());
            } else {
                Value hearingVenueValue = getHearingVenueValue(
                    hearingLocation, hearingLocations.get(0).getLocationId());
                asylumCase.write(
                    CHANGE_HEARING_VENUE,
                    HearingCentre.getHearingCentreByEpimsId(hearingVenueValue.getCode()).getValue());
                hearingLocation.setValue(hearingVenueValue);
                asylumCase.write(HEARING_LOCATION, hearingLocation);
            }
        }
    }

    private Value getHearingVenueValue(DynamicList hearingLocation, String hearingVenueId) {
        return hearingLocation.getListItems().stream().filter(location -> Objects.equals(
            location.getCode(),
            hearingVenueId
        )).findFirst().orElse(hearingLocation.getValue());
    }

    private void setHearingDurationDetails(AsylumCase asylumCase, HearingDetails hearingDetails) {
        String hearingLengthStr = asylumCase.read(LIST_CASE_HEARING_LENGTH, String.class).orElse(null);

        Optional<HearingLength> hearingLengthOptional = hearingLengthStr != null && !hearingLengthStr.isEmpty()
            ?  HearingLength.from(Integer.parseInt(hearingLengthStr))
            : HearingLength.from(hearingDetails.getDuration());

        hearingLengthOptional.ifPresent(hearingLength -> {
            asylumCase.write(CHANGE_HEARING_DURATION, hearingLength.convertToHourMinuteString());
            asylumCase.write(REQUEST_HEARING_LENGTH, String.valueOf(hearingLength.getValue()));
        });
    }

    private void setHearingDateDetails(AsylumCase asylumCase, HearingWindowModel hearingWindow) {
        boolean hearingDateDetailsSet = setHearingDateDetailsFromListedHearing(asylumCase);

        if (!(hearingDateDetailsSet || hearingWindow == null)) {
            hearingDateDetailsSet = setHearingDateDetailsFromRequestedHearing(asylumCase, hearingWindow);
        }

        if (!hearingDateDetailsSet) {
            asylumCase.write(CHANGE_HEARING_DATE, "First available date");
        }
    }

    private boolean setHearingDateDetailsFromListedHearing(AsylumCase asylumCase) {
        String listCaseHearingDate = asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse("");
        if (listCaseHearingDate.isEmpty()) {
            return false;
        }

        LocalDateTime dateTime = LocalDateTime.parse(listCaseHearingDate);
        asylumCase.write(
            REQUEST_HEARING_DATE,
            dateTime
        );
        asylumCase.write(
            CHANGE_HEARING_DATE,
            HearingsUtils.convertToLocalStringFormat(dateTime)
        );

        return true;
    }

    private boolean setHearingDateDetailsFromRequestedHearing(AsylumCase asylumCase, HearingWindowModel hearingWindow) {
        boolean hearingDateSet = false;
        final StringBuilder dateRangeBuilder = new StringBuilder("Choose a date range");

        if (hearingWindow.getFirstDateTimeMustBe() != null) {
            LocalDateTime firstDateTime = LocalDateTime.parse(hearingWindow.getFirstDateTimeMustBe());
            asylumCase.write(REQUEST_HEARING_DATE, firstDateTime);
            String dateStr = "Date to be fixed: " + HearingsUtils.convertToLocalStringFormat(firstDateTime);
            asylumCase.write(CHANGE_HEARING_DATE, dateStr);

            hearingDateSet = true;
        } else if (!(hearingWindow.getDateRangeStart() == null && hearingWindow.getDateRangeEnd() == null)) {
            if (hearingWindow.getDateRangeStart() != null) {
                dateRangeBuilder.append(": Earliest ");
                LocalDateTime earliestDate = HearingsUtils.convertToLocalDateFormat(hearingWindow.getDateRangeStart());
                dateRangeBuilder.append(HearingsUtils.convertToLocalStringFormat(earliestDate));
                asylumCase.write(CHANGE_HEARING_DATE_RANGE_EARLIEST, hearingWindow.getDateRangeStart());
            }
            if (hearingWindow.getDateRangeEnd() != null) {
                dateRangeBuilder.append(": Latest ");
                LocalDateTime latestDate = HearingsUtils.convertToLocalDateFormat(hearingWindow.getDateRangeEnd());
                dateRangeBuilder.append(HearingsUtils.convertToLocalStringFormat(latestDate));
                asylumCase.write(CHANGE_HEARING_DATE_RANGE_LATEST, hearingWindow.getDateRangeEnd());
            }
            asylumCase.write(CHANGE_HEARING_DATE, dateRangeBuilder.toString());

            hearingDateSet = true;
        }

        return hearingDateSet;
    }
}
