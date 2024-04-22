package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.ONPPRS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.ListingEvent;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;

@Slf4j
@RequiredArgsConstructor
public class ListedHearingService {

    private final LocationRefDataService locationRefDataService;

    public boolean isSubstantiveListedHearing(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.LISTED)
            && isHearingListingStatus(serviceData, ListingStatus.FIXED)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, SUBSTANTIVE);
    }

    public boolean isSubstantiveCancelledHearing(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.CANCELLED)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, SUBSTANTIVE);
    }

    public boolean isBailListedHearing(ServiceData serviceData) {
        return isHmcStatus(serviceData, HmcStatus.LISTED)
            && isHearingListingStatus(serviceData, ListingStatus.FIXED)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, BAIL);
    }

    public String getCaseReference(ServiceData serviceData) {
        return serviceData.read(CASE_REF, String.class)
            .orElseThrow(() -> new IllegalStateException("Case reference can not be null"));
    }

    public void updateListCaseHearingDetails(ServiceData serviceData, AsylumCase asylumCase) {

        List<HearingChannel> hearingChannels = getHearingChannels(serviceData);
        String hearingVenueId = getHearingVenueId(serviceData);

        String newHearingDateTime = formatHearingDateTime(getHearingDatetime(serviceData, hearingVenueId));
        HearingCentre newHearingCentre = getHearingCenter(hearingChannels, hearingVenueId);

        asylumCase.write(ARIA_LISTING_REFERENCE, getListingReference());
        asylumCase.write(LIST_CASE_HEARING_DATE, newHearingDateTime);
        asylumCase.write(LISTING_LENGTH, new HoursMinutes(getHearingDuration(serviceData)));
        asylumCase.write(LIST_CASE_HEARING_CENTRE,
                         newHearingCentre);
        if (!newHearingCentre.getEpimsId().isEmpty()) {
            asylumCase.write(LIST_CASE_HEARING_CENTRE_ADDRESS, getHearingCenterAddress(newHearingCentre));
        } else {
            asylumCase.write(LIST_CASE_HEARING_CENTRE_ADDRESS, "Remote hearing");
        }
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

    public String formatHearingDate(LocalDateTime hearingDatetime) {
        return hearingDatetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public LocalDateTime getHearingDatetime(
        ServiceData serviceData, String hearingVenueId) {
        LocalDateTime hearingDateTime = serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("nextHearingDate can not be null"));

        return HandlerUtils.getHearingDateAndTime(
            hearingDateTime.truncatedTo(ChronoUnit.MINUTES), hearingVenueId);
    }

    public LocalDateTime getBailHearingDatetime(ServiceData serviceData) {
        return serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("nextHearingDate can not be null"));
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
            && !isHearingChannel(serviceData, ONPPRS)
            && serviceData.read(HEARING_TYPE, String.class)
            .map(hearingType -> Objects.equals(hearingType, CASE_MANAGEMENT_REVIEW.getKey()))
            .orElse(false);
    }

    public void updateInitialBailCaseListing(ServiceData serviceData, BailCase bailCase) {
        List<HearingChannel> hearingChannels = getHearingChannels(serviceData);
        final String nextHearingChannel = hearingChannels.get(0).name();
        final boolean isRemoteHearing = nextHearingChannel.equals(VID.name()) || nextHearingChannel.equals(TEL.name());
        final String nextHearingVenueId = isRemoteHearing
            ? REMOTE_HEARING.getEpimsId() : getHearingVenueId(serviceData);
        LocalDateTime hearingDateTime = getBailHearingDatetime(serviceData);
        HearingCentre newHearingCentre = getHearingCenter(hearingChannels, nextHearingVenueId);

        bailCase.write(LISTING_EVENT, ListingEvent.INITIAL_LISTING.toString());
        bailCase.write(LISTING_HEARING_DATE, formatHearingDateTime(hearingDateTime));
        bailCase.write(LISTING_HEARING_DURATION, String.valueOf(getHearingDuration(serviceData)));
        bailCase.write(LISTING_LOCATION, newHearingCentre.getValue());
    }

    public void updateRelistingBailCaseListing(ServiceData serviceData, BailCase bailCase,
                                               Set<ServiceDataFieldDefinition> fieldsToUpdate) {

        if (fieldsToUpdate.contains(NEXT_HEARING_DATE)) {
            LocalDateTime hearingDateTime = getBailHearingDatetime(serviceData);
            bailCase.write(LISTING_HEARING_DATE, formatHearingDateTime(hearingDateTime));
        }

        if (fieldsToUpdate.contains(HEARING_CHANNELS) || fieldsToUpdate.contains(HEARING_VENUE_ID)) {
            List<HearingChannel> hearingChannels = getHearingChannels(serviceData);
            final String nextHearingChannel = hearingChannels.get(0).name();
            final boolean isRemoteHearing = nextHearingChannel.equals(VID.name())
                                            || nextHearingChannel.equals(TEL.name());
            final String nextHearingVenueId = isRemoteHearing
                ? REMOTE_HEARING.getEpimsId() : getHearingVenueId(serviceData);
            HearingCentre newHearingCentre = getHearingCenter(hearingChannels, nextHearingVenueId);

            bailCase.write(LISTING_LOCATION, newHearingCentre.getValue());
        }

        if (fieldsToUpdate.contains(DURATION)) {
            bailCase.write(LISTING_HEARING_DURATION, String.valueOf(getHearingDuration(serviceData)));
        }

        bailCase.write(LISTING_EVENT, ListingEvent.RELISTING.toString());
    }

    public String getHearingCenterAddress(HearingCentre hearingCentre) {
        return locationRefDataService.getCourtVenues()
            .stream()
            .filter(courtVenue -> StringUtils.equals(courtVenue.getEpimmsId(), hearingCentre.getEpimsId()))
            .findFirst()
            .map(this::assembleCourtVenueAddress)
            .orElse("");
    }

    private String assembleCourtVenueAddress(CourtVenue courtVenue) {
        return defaultIfNull(courtVenue.getCourtName(), "") + ", "
               + defaultIfNull(courtVenue.getCourtAddress(), "") + ", "
               + defaultIfNull(courtVenue.getPostcode(), "");
    }

}

