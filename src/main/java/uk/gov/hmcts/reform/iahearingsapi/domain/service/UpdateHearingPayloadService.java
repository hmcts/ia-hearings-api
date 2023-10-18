package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UpdateHearingPayloadService {

    HearingService hearingService;

    UpdateHearingPayloadService(
        HearingService hearingService
    ) {
        this.hearingService = hearingService;
    }

    public UpdateHearingRequest createUpdateHearingPayload(
        String hearingId,
        Optional<String> hearingChannels,
        Optional<String> locationCode,
        Optional<Integer> duration,
        String reasonCode,
        Boolean firstAvailableDate,
        HearingWindowModel hearingWindowModel
    ) {
        HearingGetResponse persistedHearing = hearingService.getHearing(hearingId);

        HearingDetails hearingDetails = HearingDetails.builder()
            .hearingChannels(getHearingChannels(hearingChannels, persistedHearing))
            .hearingLocations(getLocations(locationCode, persistedHearing))
            .duration(getDuration(duration, persistedHearing))
            .amendReasonCodes(List.of(reasonCode))
            .hearingWindow(updateHearingWindow(
                firstAvailableDate,
                hearingWindowModel,
                persistedHearing
            ))
            .build();


        return UpdateHearingRequest.builder()
            .requestDetails(persistedHearing.getRequestDetails())
            .caseDetails(persistedHearing.getCaseDetails())
            .hearingDetails(buildHearingDetails(persistedHearing.getHearingDetails(), hearingDetails))
            .partyDetails(persistedHearing.getPartyDetails())
            .build();
    }

    private List<String> getHearingChannels(Optional<String> hearingChannels, HearingGetResponse persistedHearing) {
        return hearingChannels.map(List::of).orElseGet(() -> persistedHearing.getHearingDetails().getHearingChannels());
    }

    private List<HearingLocationModel> getLocations(Optional<String> locationCodeOptional,
                                                    HearingGetResponse persistedHearing) {
        return locationCodeOptional.map(locationCode ->
                                            List.of(HearingLocationModel.builder()
                                                        .locationId(locationCode)
                                                        .locationType(persistedHearing
                                                                          .getHearingDetails()
                                                                          .getHearingLocations().get(0)
                                                                          .getLocationType()).build()))
            .orElseGet(() -> persistedHearing.getHearingDetails().getHearingLocations());
    }

    private Integer getDuration(Optional<Integer> duration, HearingGetResponse persistedHearing) {
        return duration.orElseGet(() -> persistedHearing.getHearingDetails().getDuration());
    }

    private HearingWindowModel updateHearingWindow(boolean firstAvailable,
                                                   HearingWindowModel hearingWindowModel,
                                                   HearingGetResponse persistedHearing) {
        if (!firstAvailable) {
            if (hearingWindowModel == null) {
                return persistedHearing.getHearingDetails().getHearingWindow();
            } else {
                return hearingWindowModel;
            }
        }
        return null;
    }


    private HearingDetails buildHearingDetails(HearingDetails hearingDetails, HearingDetails updatedHearingsDetails) {
        hearingDetails.setHearingChannels(updatedHearingsDetails.getHearingChannels());
        hearingDetails.setHearingLocations(updatedHearingsDetails.getHearingLocations());
        hearingDetails.setDuration(updatedHearingsDetails.getDuration());
        hearingDetails.setAmendReasonCodes(updatedHearingsDetails.getAmendReasonCodes());
        hearingDetails.setHearingWindow(updatedHearingsDetails.getHearingWindow());
        return hearingDetails;
    }
}