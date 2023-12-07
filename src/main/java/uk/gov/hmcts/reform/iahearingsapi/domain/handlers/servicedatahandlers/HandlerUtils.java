package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_LISTING_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_TYPE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HMC_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.LIST_ASSIST_CASE_STATUS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.INTER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;

public class HandlerUtils {
    private HandlerUtils() {
    }

    private static final String GLASGOW_EPIMMS_ID = "366559";

    public static boolean isHmcStatus(ServiceData serviceData, HmcStatus benchMark) {
        return serviceData.read(HMC_STATUS, HmcStatus.class)
            .map(hmcStatus -> Objects.equals(hmcStatus, benchMark))
            .orElse(false);
    }

    public static boolean isHearingListingStatus(ServiceData serviceData, ListingStatus benchMark) {
        return serviceData.read(HEARING_LISTING_STATUS, ListingStatus.class)
            .map(listingStatus -> Objects.equals(listingStatus, benchMark))
            .orElse(false);
    }

    public static boolean isListAssistCaseStatus(ServiceData serviceData, ListAssistCaseStatus benchMark) {
        return serviceData.read(LIST_ASSIST_CASE_STATUS, ListAssistCaseStatus.class)
            .map(listAssistCaseStatus -> Objects.equals(listAssistCaseStatus, benchMark))
            .orElse(false);
    }

    public static boolean isHearingChannel(ServiceData serviceData, HearingChannel benchMark) {
        return serviceData.read(HEARING_CHANNELS, List.class)
            .map(hearingChannels -> hearingChannels.contains(benchMark))
            .orElse(false);
    }

    public static boolean isHearingType(ServiceData serviceData, HearingType benchMark) {
        return serviceData.read(HEARING_TYPE, String.class)
            .map(hearingType -> Objects.equals(hearingType, benchMark.getKey()))
            .orElse(false);
    }

    public static LocalDateTime getHearingDateAndTime(LocalDateTime hearingDateTime,
                                                      List<HearingChannel> hearingChannels,
                                                      String venueId) {
        if (hearingChannels.contains(INTER)) {
            return StringUtils.equals(venueId, GLASGOW_EPIMMS_ID)
                ? hearingDateTime.with(LocalTime.of(9, 45))
                : hearingDateTime.with(LocalTime.of(10, 0));
        } else {
            return hearingDateTime;
        }
    }

    public static HearingCentre getLocation(List<HearingChannel> hearingChannels, String venueId) {
        if (!hearingChannels.contains(INTER)
            && (hearingChannels.contains(VID) || hearingChannels.contains(TEL))) {
            return REMOTE_HEARING;
        }
        return HearingCentre.getHearingCentreByEpimsId(venueId);
    }
}
