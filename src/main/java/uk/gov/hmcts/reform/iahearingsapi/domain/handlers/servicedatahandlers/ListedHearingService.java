package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LIST;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseHearing;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;

@Slf4j
public class ListedHearingService {
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

    public void updateListCaseHearingDetails(ServiceData serviceData, AsylumCase asylumCase,
                                             boolean isAppealsLocationRefDataEnabled, String caseId,
                                             List<CourtVenue> courtVenues, DynamicList hearingLocationList) {

        List<HearingChannel> hearingChannels = getHearingChannels(serviceData);
        String hearingVenueId = getHearingVenueId(serviceData);

        String newHearingDateTime = formatHearingDateTime(getHearingDatetime(serviceData, hearingVenueId));
        HearingCentre newHearingCentre = HandlerUtils.getLocation(hearingChannels, hearingVenueId);
        DynamicList newHearingChannel = buildHearingChannelDynmicList(hearingChannels);

        asylumCase.write(ARIA_LISTING_REFERENCE, getListingReference());
        asylumCase.write(LIST_CASE_HEARING_DATE, newHearingDateTime);
        asylumCase.write(LISTING_LENGTH, new HoursMinutes(getHearingDuration(serviceData)));
        asylumCase.write(LIST_CASE_HEARING_CENTRE, newHearingCentre);
        asylumCase.write(HEARING_CHANNEL, newHearingChannel);

        if (isAppealsLocationRefDataEnabled) {
            asylumCase.write(AsylumCaseFieldDefinition.IS_REMOTE_HEARING, isRemoteHearing(serviceData) ? YES : NO);
            log.info("updateListCaseHearingDetails for Case ID `{}` serviceData contains '{}", caseId, serviceData);

            asylumCase.write(AsylumCaseFieldDefinition.LISTING_LOCATION,
                new DynamicList(
                    new Value(getHearingVenueId(serviceData), getHearingCourtName(serviceData, courtVenues)),
                    hearingLocationList.getListItems()));

            log.info("updateListCaseHearingDetails for Case ID `{}` listingLocation contains '{}'", caseId,
                asylumCase.read(AsylumCaseFieldDefinition.LISTING_LOCATION).toString());
        }

        Optional<List<AsylumCaseHearing>> hearingsOpt = asylumCase.read(AsylumCaseFieldDefinition.HEARING_LIST);
        List<AsylumCaseHearing> hearings = hearingsOpt.orElse(new ArrayList<AsylumCaseHearing>());
        String hearingId = serviceData.read(HEARING_ID, String.class)
                .orElseThrow(() -> new IllegalStateException("hearing id can not be null"));
        Optional<AsylumCaseHearing> existingHearingOpt = getHearingFromAsylumCase(hearings, hearingId);
        if (existingHearingOpt.isPresent()) {
            AsylumCaseHearing hearing = existingHearingOpt.get();
            hearing.setNextHearingDate(newHearingDateTime);
        } else {
            AsylumCaseHearing newHearing = new AsylumCaseHearing(hearingId, newHearingDateTime, null);
            hearings.add(newHearing);
        }

        asylumCase.write(HEARING_LIST, hearings);

        log.info("Called updateListCaseHearingDetails for  Case ID `{}`, asylumCase '{}'",
                caseId, asylumCase.toString());
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

    public void updateInitialBailCaseListing(ServiceData serviceData, BailCase bailCase,
                                             boolean isRefDataLocationEnabled, String caseId,
                                             List<CourtVenue> courtVenues, DynamicList hearingLocationList) {
        LocalDateTime hearingDateTime = getBailHearingDatetime(serviceData);

        bailCase.write(LISTING_EVENT, ListingEvent.INITIAL_LISTING.toString());
        bailCase.write(LISTING_HEARING_DATE, formatHearingDateTime(hearingDateTime));
        bailCase.write(LISTING_HEARING_DURATION, String.valueOf(getHearingDuration(serviceData)));
        bailCase.write(LISTING_LOCATION, getHearingCentre(serviceData).getValue());

        if (isRefDataLocationEnabled) {
            bailCase.write(IS_REMOTE_HEARING, isRemoteHearing(serviceData) ? YES : NO);
            log.info("updateInitialBailCaseListing for Case ID `{}` serviceData contains '{}", caseId, serviceData);

            bailCase.write(REF_DATA_LISTING_LOCATION,
                new DynamicList(
                    new Value(getHearingVenueId(serviceData), getHearingCourtName(serviceData, courtVenues)),
                    hearingLocationList.getListItems()));

            log.info("updateInitialBailCaseListing for Case ID `{}` listingLocation contains '{}'", caseId,
                bailCase.read(REF_DATA_LISTING_LOCATION).toString());
        }
    }

    public void updateRelistingBailCaseListing(ServiceData serviceData, BailCase bailCase,
                                               Set<ServiceDataFieldDefinition> fieldsToUpdate,
                                               boolean isRefDataLocationEnabled, List<CourtVenue> courtVenues,
                                               DynamicList hearingLocationList) {

        if (fieldsToUpdate.contains(NEXT_HEARING_DATE)) {
            LocalDateTime hearingDateTime = getBailHearingDatetime(serviceData);
            bailCase.write(LISTING_HEARING_DATE, formatHearingDateTime(hearingDateTime));
        }

        if (fieldsToUpdate.contains(HEARING_CHANNELS) || fieldsToUpdate.contains(HEARING_VENUE_ID)) {
            bailCase.write(LISTING_LOCATION, getHearingCentre(serviceData).getValue());
        }

        if (isRefDataLocationEnabled) {
            if (fieldsToUpdate.contains(HEARING_VENUE_ID)) {
                bailCase.write(REF_DATA_LISTING_LOCATION,
                    new DynamicList(
                        new Value(getHearingVenueId(serviceData),
                            getHearingCourtName(serviceData, courtVenues)),
                        hearingLocationList.getListItems()));
            }

            if (fieldsToUpdate.contains(HEARING_CHANNELS)) {
                bailCase.write(IS_REMOTE_HEARING, isRemoteHearing(serviceData) ? YES : NO);
            }
        }

        if (fieldsToUpdate.contains(DURATION)) {
            bailCase.write(LISTING_HEARING_DURATION, String.valueOf(getHearingDuration(serviceData)));
        }

        bailCase.write(LISTING_EVENT, ListingEvent.RELISTING.toString());
    }

    public Set<ServiceDataFieldDefinition> findUpdatedServiceDataFields(
        ServiceData serviceData,
        List<PartiesNotifiedResponse> partiesNotifiedResponses,
        Set<ServiceDataFieldDefinition> targetFields) {

        ServiceData previousServiceData = partiesNotifiedResponses.stream()
            .max(Comparator.comparing(PartiesNotifiedResponse::getResponseReceivedDateTime, LocalDateTime::compareTo))
            .map(PartiesNotifiedResponse::getServiceData)
            .orElseGet(ServiceData::new);

        return targetFields.stream()
            .filter(field -> fieldUpdated(previousServiceData, serviceData, field))
            .collect(Collectors.toSet());
    }

    protected Optional<AsylumCaseHearing> getHearingFromAsylumCase(
            List<AsylumCaseHearing> hearings,
            String hearingId
    ) {
        for (AsylumCaseHearing hearing: hearings) {
            if (hearing.getHearingId().equals(hearingId)) {
                return Optional.of(hearing);
            }
        }
        return Optional.empty();
    }

    private boolean fieldUpdated(ServiceData previous, ServiceData latest, ServiceDataFieldDefinition field) {

        if (field == HEARING_CHANNELS) {
            Optional<List<HearingChannel>> previousOptionalHearingChannels = previous.read(HEARING_CHANNELS);
            Optional<List<HearingChannel>> latestOptionalHearingChannels = latest.read(HEARING_CHANNELS);
            List<HearingChannel> previousHearingChannels = previousOptionalHearingChannels
                .orElse(Collections.emptyList());
            List<HearingChannel> latestHearingChannels = latestOptionalHearingChannels
                .orElse(Collections.emptyList());

            return !((previousHearingChannels.size() == latestHearingChannels.size())
                     && previousHearingChannels.containsAll(latestHearingChannels));
        }

        return !Objects.equals(previous.read(field).orElse(null), latest.read(field).orElse(null));
    }

    private HearingCentre getHearingCentre(ServiceData serviceData) {

        return isRemoteHearing(serviceData)
            ? REMOTE_HEARING
            : HearingCentre.getHearingCentreByEpimsId(getHearingVenueId(serviceData));
    }

    public boolean isRemoteHearing(ServiceData serviceData) {
        final String nextHearingChannel = getHearingChannels(serviceData).get(0).name();
        return nextHearingChannel.equals(VID.name()) || nextHearingChannel.equals(TEL.name());
    }

    public String getHearingCourtName(ServiceData serviceData, List<CourtVenue> courtVenues) {
        return courtVenues.stream()
            .filter(c -> c.getEpimmsId().equals(getHearingVenueId(serviceData)))
            .map(CourtVenue::getCourtName)
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No matching ref data court venue found for epims id "
                + getHearingVenueId(serviceData)));
    }
}

