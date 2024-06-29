package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ADJOURNMENT_DETAILS_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_VENUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.exception.HmcException;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecordAdjournmentDetailsMidEventHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingService hearingService;

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && callback.getEvent() == RECORD_ADJOURNMENT_DETAILS;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String hearingId = asylumCase.read(ADJOURNMENT_DETAILS_HEARING, DynamicList.class)
            .map(hearings -> hearings.getValue().getCode()).orElse(null);

        if (hearingId != null) {
            try {
                asylumCase.read(NEXT_HEARING_VENUE, DynamicList.class).ifPresent(nextHearingVenue ->
                    getHearingLocation(hearingId).ifPresent(location -> {
                        Value nextHearingVenueValue = nextHearingVenue.getListItems().stream()
                            .filter(venue -> Objects.equals(venue.getCode(), location.getLocationId()))
                            .findFirst().orElse(nextHearingVenue.getValue());
                        nextHearingVenue.setValue(nextHearingVenueValue);
                        asylumCase.write(NEXT_HEARING_VENUE, nextHearingVenue);
                    })
                );
            } catch (HmcException ex) {
                log.error(ex.getMessage());
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Optional<HearingLocationModel> getHearingLocation(String hearingId) {

        return getHearingDetails(hearingId)
            .map(HearingDetails::getHearingLocations)
            .orElseGet(Collections::emptyList)
            .stream().findFirst();
    }

    private Optional<HearingDetails> getHearingDetails(String hearingId) {
        HearingGetResponse getHearingResponse = hearingService.getHearing(hearingId);

        return getHearingResponse == null ? Optional.empty() : Optional.of(getHearingResponse.getHearingDetails());
    }
}
