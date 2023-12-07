package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.ONPPRS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.CASE_MANAGEMENT_REVIEW;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingChannel;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingListingStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingType;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHmcStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;

@Slf4j
public class SubstantiveListedHearingService {

    public boolean isSubstantiveListedHearing(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.LISTED)
            && isHearingListingStatus(serviceData, ListingStatus.FIXED)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, SUBSTANTIVE);
    }

    public String getCaseReference(ServiceData serviceData) {
        return serviceData.read(CASE_REF, String.class)
            .orElseThrow(() -> new IllegalStateException("Case reference can not be null"));
    }

    public void updateListCaseHearingDetails(ServiceData serviceData, AsylumCase asylumCase) {

        List<HearingChannel> hearingChannels = getHearingChannels(serviceData);
        String hearingVenueId = getHearingVenueId(serviceData);

        asylumCase.write(ARIA_LISTING_REFERENCE, getListingReference());
        asylumCase.write(LIST_CASE_HEARING_DATE, formatHearingDateTime(
            getHearingDatetime(serviceData, hearingChannels, hearingVenueId)));
        asylumCase.write(LIST_CASE_HEARING_LENGTH,
                         String.valueOf(getHearingDuration(serviceData)));
        asylumCase.write(LIST_CASE_HEARING_CENTRE,
                         getHearingCenter(hearingChannels, hearingVenueId));
        asylumCase.write(HEARING_CHANNEL, buildHearingChannelDynmicList(hearingChannels));
    }

    public List<HearingChannel> getHearingChannels(ServiceData serviceData) {
        Optional<List<HearingChannel>> optionalHearingChannels = serviceData.read(HEARING_CHANNELS);

        return optionalHearingChannels
            .orElseThrow(() -> new IllegalStateException("hearingChannels can not be empty"));
    }

    public String getHearingVenueId(ServiceData serviceData) {
        return serviceData.read(HEARING_VENUE_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("hearingVenueId can not be null"));
    }

    public String formatHearingDateTime(LocalDateTime hearingDatetime) {
        return hearingDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public LocalDateTime getHearingDatetime(
        ServiceData serviceData, List<HearingChannel> hearingChannels, String hearingVenueId) {
        LocalDateTime hearingDateTime = serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("nextHearingDate can not be null"));

        return HandlerUtils.getHearingDateAndTime(
            hearingDateTime.truncatedTo(ChronoUnit.MINUTES), hearingChannels, hearingVenueId);
    }

    public int getHearingDuration(ServiceData serviceData) {
        return serviceData.read(DURATION, Integer.class)
            .orElseThrow(() -> new IllegalStateException("duration can not be null"));
    }

    public String getListingReference() {
        return "LAI";
    }

    public HearingCentre getHearingCenter(List<HearingChannel> hearingChannels, String hearingVenueId) {
        return HandlerUtils.getLocation(hearingChannels, hearingVenueId);
    }

    public DynamicList buildHearingChannelDynmicList(List<HearingChannel> hearingChannels) {
        return new DynamicList(new Value(
            hearingChannels.get(0).name(),
            hearingChannels.get(0).getLabel()
        ), List.of(new Value(
            hearingChannels.get(0).name(),
            hearingChannels.get(0).getLabel()
        )));
    }

    public boolean isCaseManagementReview(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.LISTED)
            && isHearingListingStatus(serviceData, ListingStatus.FIXED)
            && !isHearingChannel(serviceData, ONPPRS)
            && serviceData.read(HEARING_TYPE, String.class)
            .map(hearingType -> Objects.equals(hearingType, CASE_MANAGEMENT_REVIEW.getKey()))
            .orElse(false);
    }
}

