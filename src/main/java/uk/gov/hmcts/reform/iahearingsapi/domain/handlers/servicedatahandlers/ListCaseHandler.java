package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.CASE_REF;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.ONPPRS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingChannel;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingListingStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHearingType;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isHmcStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;


@Slf4j
@Component
@RequiredArgsConstructor
public class ListCaseHandler implements ServiceDataHandler<ServiceData> {

    private static final String LISTING_REFERENCE = "LAI";
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
            && isListAssistCaseStatus(serviceData, ListAssistCaseStatus.LISTED)
            && !isHearingChannel(serviceData, ONPPRS)
            && isHearingType(serviceData, SUBSTANTIVE);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        final String caseId = serviceData.read(CASE_REF, String.class)
            .orElseThrow(() -> new IllegalStateException("Case reference can not be null"));

        Optional<List<HearingChannel>> optionalHearingChannels = serviceData.read(HEARING_CHANNELS);
        List<HearingChannel> hearingChannels = optionalHearingChannels
            .orElseThrow(() -> new IllegalStateException("hearingChannels can not be empty"));

        LocalDateTime nextHearingDate = serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("nextHearingDate can not be null"));

        String hearingVenueId = serviceData.read(HEARING_VENUE_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("hearingVenueId can not be null"));

        int duration = serviceData.read(DURATION, Integer.class)
            .orElseThrow(() -> new IllegalStateException("duration can not be null"));
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(ARIA_LISTING_REFERENCE.value(), LISTING_REFERENCE);
        caseData.put(LIST_CASE_HEARING_DATE.value(),
                         HandlerUtils.getHearingDateAndTime(nextHearingDate, hearingChannels, hearingVenueId)
                             .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
        caseData.put(LIST_CASE_HEARING_LENGTH.value(), String.valueOf(duration));
        caseData.put(LIST_CASE_HEARING_CENTRE.value(), HandlerUtils.getLocation(hearingChannels, hearingVenueId));

        log.info("Sending `{}` event for  Case ID `{}`", LIST_CASE, caseId);
        coreCaseDataService.triggerEvent(LIST_CASE, caseId, caseData);

        return new ServiceDataResponse<>(serviceData);
    }
}

