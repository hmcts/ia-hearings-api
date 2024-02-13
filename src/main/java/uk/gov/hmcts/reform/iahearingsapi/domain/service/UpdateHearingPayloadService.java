package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.REQUEST_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.NEXT_HEARING_VENUE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.Facilities.IAC_TYPE_C_CONFERENCE_EQUIPMENT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingGetResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingLocationModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingWindowModel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.UpdateHearingRequest;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseDataToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.CaseFlagsToServiceHearingValuesMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.ListingCommentsMapper;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.MapperUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.mappers.PartyDetailsMapper;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Component
@Slf4j
public class UpdateHearingPayloadService extends CreateHearingPayloadService {

    private HearingService hearingService;

    public UpdateHearingPayloadService(CaseDataToServiceHearingValuesMapper caseDataMapper,
                                       CaseFlagsToServiceHearingValuesMapper caseFlagsMapper,
                                       PartyDetailsMapper partyDetailsMapper,
                                       ListingCommentsMapper listingCommentsMapper,
                                       @Value("${hearingValues.hmctsServiceId}") String serviceId,
                                       @Value("${xui.api.baseUrl}") String baseUrl,
                                       HearingService hearingService) {
        super(caseDataMapper,
              caseFlagsMapper,
              partyDetailsMapper,
              listingCommentsMapper,
              serviceId,
              baseUrl);
        this.hearingService = hearingService;
    }

    public UpdateHearingRequest createUpdateHearingPayload(
        AsylumCase asylumCase,
        String hearingId,
        String reasonCode,
        Boolean firstAvailableDate,
        HearingWindowModel hearingWindowModel,
        Event event
    ) {
        return generateHearingPayload(
            asylumCase,
            hearingId,
            reasonCode,
            firstAvailableDate,
            hearingWindowModel,
            event);
    }

    public UpdateHearingRequest createUpdateHearingPayload(
        AsylumCase asylumCase,
        String hearingId,
        String reasonCode,
        Event event
    ) {
        return generateHearingPayload(asylumCase, hearingId, reasonCode, false, null, event);
    }

    private UpdateHearingRequest generateHearingPayload(
        AsylumCase asylumCase,
        String hearingId,
        String reasonCode,
        Boolean firstAvailableDate,
        HearingWindowModel hearingWindowModel,
        Event event
    ) {
        HearingGetResponse persistedHearing = hearingService.getHearing(hearingId);

        HearingDetails hearingDetails = HearingDetails.builder()
            .autolistFlag(getAutoListFlag(asylumCase, persistedHearing.getHearingDetails()))
            .hearingChannels(caseDataMapper.getHearingChannels(asylumCase, persistedHearing.getHearingDetails(), event))
            .hearingLocations(getLocations(asylumCase, persistedHearing, event))
            .duration(getDuration(asylumCase, persistedHearing, event))
            .amendReasonCodes(List.of(reasonCode))
            .hearingWindow(updateHearingWindow(firstAvailableDate, hearingWindowModel, persistedHearing))
            .build();


        UpdateHearingRequest updatedHearingRequest = UpdateHearingRequest.builder()
            .requestDetails(persistedHearing.getRequestDetails())
            .caseDetails(persistedHearing.getCaseDetails())
            .hearingDetails(buildHearingDetails(asylumCase, persistedHearing.getHearingDetails(), hearingDetails))
            .partyDetails(getPartyDetailsModels(asylumCase, persistedHearing.getHearingDetails(), event))
            .build();

        log.info("Updated hearing request to be persisted: {}", updatedHearingRequest.toString());
        return updatedHearingRequest;
    }

    private boolean getAutoListFlag(AsylumCase asylumCase, HearingDetails persistedHearingDetails) {
        return !caseDataMapper.isDecisionWithoutHearingAppeal(asylumCase) && persistedHearingDetails.isAutolistFlag();
    }

    private List<HearingLocationModel> getLocations(AsylumCase asylumCase,
                                                    HearingGetResponse persistedHearing,
                                                    Event event) {

        Optional<String> locationCodes = Optional.empty();
        if (event != null) {
            switch (event) {
                case RECORD_ADJOURNMENT_DETAILS:
                    locationCodes = asylumCase.read(NEXT_HEARING_VENUE, DynamicList.class)
                        .map(hearingLocation -> hearingLocation.getValue().getCode());
                    break;

                case UPDATE_HEARING_REQUEST:
                    locationCodes = asylumCase.read(HEARING_LOCATION, DynamicList.class)
                        .map(hearingLocation -> hearingLocation.getValue().getCode());
                    break;
            }
        }

        return locationCodes.map(locationCode ->
                List.of(HearingLocationModel.builder()
                    .locationId(locationCode)
                    .locationType(persistedHearing
                        .getHearingDetails()
                        .getHearingLocations().get(0)
                        .getLocationType()).build()))
            .orElseGet(() -> persistedHearing.getHearingDetails().getHearingLocations());
    }

    private Integer getDuration(AsylumCase asylumCase,
                                HearingGetResponse persistedHearing,
                                Event event) {
        return defaultIfNull(getHearingDuration(asylumCase, event),
            persistedHearing.getHearingDetails().getDuration());
    }

    public Integer getHearingDuration(AsylumCase asylumCase, Event event) {
        if (caseDataMapper.isDecisionWithoutHearingAppeal(asylumCase)) {
            return null;
        }

        int hearingDuration = 0;
        if (event != null) {
            switch (event) {
                case RECORD_ADJOURNMENT_DETAILS:
                    hearingDuration = caseDataMapper
                        .getIntHearingDurationFromString(asylumCase, NEXT_HEARING_DURATION);
                    break;

                case UPDATE_HEARING_REQUEST:
                    hearingDuration = caseDataMapper
                        .getIntHearingDurationFromString(asylumCase, REQUEST_HEARING_LENGTH);
                    break;
            }
        }
        return hearingDuration <= 0 ? null : hearingDuration;
    }

    private HearingWindowModel updateHearingWindow(boolean firstAvailable,
                                                   HearingWindowModel hearingWindowModel,
                                                   HearingGetResponse persistedHearing) {
        if (!firstAvailable) {
            if (hearingWindowModel == null) {
                return returnValidHearingWindow(persistedHearing.getHearingDetails().getHearingWindow());
            } else {
                return hearingWindowModel;
            }
        }
        return null;
    }

    private HearingWindowModel returnValidHearingWindow(HearingWindowModel hearingWindowModel) {
        if (hearingWindowModel != null) {
            if (!hearingWindowModel.allNull()) {
                return hearingWindowModel;
            }
        }
        return null;
    }

    private HearingDetails buildHearingDetails(AsylumCase asylumCase, HearingDetails hearingDetails,
                                               HearingDetails updatedHearingsDetails) {
        hearingDetails.setHearingChannels(updatedHearingsDetails.getHearingChannels());
        hearingDetails.setHearingLocations(updatedHearingsDetails.getHearingLocations());
        hearingDetails.setDuration(updatedHearingsDetails.getDuration());
        hearingDetails.setAmendReasonCodes(updatedHearingsDetails.getAmendReasonCodes());
        hearingDetails.setHearingWindow(updatedHearingsDetails.getHearingWindow());
        hearingDetails.setFacilitiesRequired(getFacilitiesRequired(
            asylumCase,
            hearingDetails.getFacilitiesRequired() != null
                ? hearingDetails.getFacilitiesRequired() : Collections.emptyList()
        ));

        return hearingDetails;
    }

    private List<String> getFacilitiesRequired(AsylumCase asylumCase, List<String> facilities) {
        List<String> filteredFacilities = new ArrayList<>(facilities);
        if (MapperUtils.isS94B(asylumCase)
            && !facilities.contains(IAC_TYPE_C_CONFERENCE_EQUIPMENT.toString())) {
            filteredFacilities.add(IAC_TYPE_C_CONFERENCE_EQUIPMENT.toString());
        } else if (!MapperUtils.isS94B(asylumCase)) {
            filteredFacilities = facilities
                .stream()
                .filter(it -> !it.equals(IAC_TYPE_C_CONFERENCE_EQUIPMENT.toString()))
                .collect(Collectors.toList());
        }
        return filteredFacilities;
    }
}
