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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    private final CoreCaseDataService coreCaseDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isHmcStatus(serviceData, HmcStatus.LISTED)
            && isHearingListingStatus(serviceData, ListingStatus.FIXED)
            && (isListAssistCaseStatus(serviceData, ListAssistCaseStatus.PREPARE_FOR_HEARING)
            || isListAssistCaseStatus(serviceData, ListAssistCaseStatus.FINAL_BUNDLING)
            || isListAssistCaseStatus(serviceData, ListAssistCaseStatus.PRE_HEARING))
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, SUBSTANTIVE);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = serviceData.read(CASE_REF, String.class)
            .orElseThrow(() -> new IllegalStateException("Case reference can not be null"));

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

        sendEditListingEventIfHearingIsUpdated(
            asylumCase,
            caseId,
            nextHearingDate,
            hearingVenueId,
            hearingChannels,
            duration
        );
        return new ServiceDataResponse<>(serviceData);
    }

    private void sendEditListingEventIfHearingIsUpdated(AsylumCase asylumCase,
                                                        String caseId,
                                                        LocalDateTime nextHearingDate,
                                                        String nextHearingVenueId,
                                                        List<HearingChannel> nextHearingChannelList,
                                                        Integer nextDuration) {
        LocalDateTime currentHearingDate = LocalDateTime.parse(asylumCase.read(
            LIST_CASE_HEARING_DATE,
            String.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingDate can not be null")));
        currentHearingDate = currentHearingDate.truncatedTo(ChronoUnit.SECONDS);
        final String currentVenueId = asylumCase.read(
            LIST_CASE_HEARING_CENTRE,
            HearingCentre.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingCentre can not be null")).getEpimsId();
        DynamicList currentHearingChannels = asylumCase.read(HEARING_CHANNEL, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException("hearingChannel can not be null"));
        final String currentHearingChannel = currentHearingChannels.getValue().getCode();
        final String currentDuration = asylumCase.read(
            LIST_CASE_HEARING_LENGTH,
            String.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingLength can not be null"));
        boolean sendUpdate = false;
        final String nextHearingChannel = nextHearingChannelList.get(0).name();
        nextHearingDate = nextHearingDate.truncatedTo(ChronoUnit.SECONDS);

        if (nextHearingChannel.equals(VID.name()) || nextHearingChannel.equals(TEL.name())) {
            nextHearingVenueId = REMOTE_HEARING.getEpimsId();
        }

        if (!currentHearingDate.equals(nextHearingDate)) {
            asylumCase.write(
                LIST_CASE_HEARING_DATE,
                HandlerUtils.getHearingDateAndTime(nextHearingDate, nextHearingChannelList, nextHearingVenueId)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
            );
            sendUpdate = true;
        }
        if (!currentHearingChannel.equals(nextHearingChannel) || !currentVenueId.equals(nextHearingVenueId)) {
            asylumCase.write(
                LIST_CASE_HEARING_CENTRE,
                HandlerUtils.getLocation(nextHearingChannelList, nextHearingVenueId)
            );
            sendUpdate = true;
        }
        if ((nextHearingChannel.equals(VID.name()) || nextHearingChannel.equals(TEL.name()))
            && !currentDuration.equals(String.valueOf(nextDuration))) {
            asylumCase.write(LIST_CASE_HEARING_LENGTH, String.valueOf(nextDuration));
            sendUpdate = true;
        }

        if (sendUpdate) {
            log.info("Sending `{}` event for case ID `{}`", EDIT_CASE_LISTING, caseId);
            coreCaseDataService.triggerEvent(EDIT_CASE_LISTING, caseId, asylumCase);
        }
    }
}

