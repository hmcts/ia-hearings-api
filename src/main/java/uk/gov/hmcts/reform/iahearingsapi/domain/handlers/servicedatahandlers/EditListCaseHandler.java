package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.ONPPRS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingChannel;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingListingStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingType;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHmcStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;


@Slf4j
@Component
@RequiredArgsConstructor
public class EditListCaseHandler implements ServiceDataHandler<ServiceData> {

    private static final String GLASGOW_EPIMMS_ID = "366559";
    private static final String LISTING_REFERENCE = "LAI";
    private final CoreCaseDataService coreCaseDataService;
    private final HearingService hearingService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isHmcStatus(serviceData, HmcStatus.LISTED)
            && isHearingListingStatus(serviceData, ListingStatus.FIXED)
            && isListAssistCaseStatus(serviceData, ListAssistCaseStatus.LISTED)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, SUBSTANTIVE);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = serviceData.read(CASE_REF, String.class)
            .orElseThrow(() -> new IllegalStateException("Case reference can not be null"));

        // CaseDetails caseDetails = coreCaseDataService.getCaseDetails(caseId);

        AsylumCase asylumCase = coreCaseDataService.getCase(caseId);

        Optional<List<HearingChannel>> optionalHearingChannels = serviceData.read(HEARING_CHANNELS);
        List<HearingChannel> hearingChannels = optionalHearingChannels
            .orElseThrow(() -> new IllegalStateException("hearingChannels can not be empty"));

        LocalDateTime nextHearingDate = serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("nextHearingDate can not be null"));

        String hearingVenueId = serviceData.read(HEARING_VENUE_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("hearingVenueId can not be null"));

        int duration = serviceData.read(DURATION, Integer.class)
            .orElseThrow(() -> new IllegalStateException("duration can not be null"));

        Boolean sendEditListingEvent = sendEditListingEventIfHearingIsUpdated(
            asylumCase,
            nextHearingDate,
            hearingVenueId,
            hearingChannels,
            duration
        );

        if (sendEditListingEvent) {
            Map<String, Object> caseData = new HashMap<>();
            caseData.put(
                LIST_CASE_HEARING_DATE.value(),
                HandlerUtils.getHearingDateAndTime(nextHearingDate, hearingChannels, hearingVenueId)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
            );
            caseData.put(LIST_CASE_HEARING_LENGTH.value(), String.valueOf(duration));
            caseData.put(LIST_CASE_HEARING_CENTRE.value(), HandlerUtils.getLocation(hearingChannels, hearingVenueId));
            log.info("Sending `{}` event for case ID `{}`", EDIT_CASE_LISTING, caseId);
            coreCaseDataService.triggerEvent(EDIT_CASE_LISTING, caseId, caseData);
        }

        return new ServiceDataResponse<>(serviceData);
    }

    private Boolean sendEditListingEventIfHearingIsUpdated(AsylumCase asylumCase,
                                                           LocalDateTime nextHearingDate,
                                                           String nextHearingVenueId,
                                                           List<HearingChannel> nextHearingChannelList,
                                                           Integer nextDuration) {
        LocalDateTime currentHearingDate = LocalDateTime.parse(asylumCase.read(
            LIST_CASE_HEARING_DATE,
            String.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingDate can not be null")));
        String currentVenueId = asylumCase.read(
            LIST_CASE_HEARING_CENTRE,
            HearingCentre.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingCentre can not be null")).getEpimsId();
        DynamicList currentHearingChannels = asylumCase.read(HEARING_CHANNEL, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("hearingChannel can not be null"));
        String currentHearingChannel = currentHearingChannels.getValue().getCode();
        String currentDuration = asylumCase.read(
            LIST_CASE_HEARING_LENGTH,
            String.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingLength can not be null"));

        String nextHearingChannel = nextHearingChannelList.get(0).name();
        currentHearingDate = currentHearingDate.truncatedTo(ChronoUnit.SECONDS);
        nextHearingDate = nextHearingDate.truncatedTo(ChronoUnit.SECONDS);

        if (nextHearingChannel.equals(VID.name()) || nextHearingChannel.equals(TEL.name())) {
            nextHearingVenueId = REMOTE_HEARING.getEpimsId();
        }

        return (!currentHearingDate.equals(nextHearingDate) ||
            !currentVenueId.equals(nextHearingVenueId) ||
            !currentHearingChannel.equals(nextHearingChannel) ||
            (nextHearingChannel.equals(VID.name()) || nextHearingChannel.equals(TEL.name()) &&
                !currentDuration.equals(String.valueOf(nextDuration))));
    }
}

