package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CMR_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CMR_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CMR_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.CMR_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CMR_RE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.isListAssistCaseStatus;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListAssistCaseStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponses;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.HearingService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmrHandler extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final HearingService hearingService;
    private final LocationRefDataService locationRefDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData) {
        requireNonNull(serviceData, "serviceData must not be null");

        return isCmrListedHearing(serviceData)
               || isCmrCancelledHearing(serviceData);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String hearingId = serviceData.read(HEARING_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("HearingID can not be missing"));
        String caseId = getCaseReference(serviceData);

        if (isCmrListedHearing(serviceData)) {
            PartiesNotifiedResponses partiesNotifiedResponses = hearingService.getPartiesNotified(hearingId);
            if (isInitialListing(hearingId, partiesNotifiedResponses.getResponses())) {
                handleCmrListing(caseId, serviceData);
            } else {
                boolean cmrHearingUpdated = isCmrUpdated(serviceData, partiesNotifiedResponses.getResponses());
                if (cmrHearingUpdated) {
                    handleCmrReListing(caseId);
                    log.info("cmrRelistingHandler triggered for hearing " + hearingId);
                } else {
                    log.info("Hearing date, channel, duration and location not updated");
                    log.info("cmrRelistingHandler not triggered for hearing " + hearingId);
                }
            }
        } else {
            handleCmrReListing(caseId);
            log.info("cmrRelistingHandler triggered for hearing " + hearingId);
        }

        return new ServiceDataResponse<>(serviceData);
    }

    private void handleCmrListing(String caseId, ServiceData serviceData) {
        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(CMR_LISTING, caseId, CASE_TYPE_ASYLUM);

        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        boolean isAppealsLocationRefDataEnabled = HearingsUtils.isAppealsLocationRefDataEnabled(asylumCase);

        updateCmrHearingDetails(serviceData, asylumCase, isAppealsLocationRefDataEnabled, caseId,
                                locationRefDataService.getCourtVenuesAsServiceUser(),
                                locationRefDataService.getHearingLocationsDynamicList(true));

        log.info("Sending `{}` event for  Case ID `{}`", CMR_LISTING, caseId);
        coreCaseDataService.triggerSubmitEvent(CMR_LISTING, caseId, startEventResponse, asylumCase);
    }

    private void handleCmrReListing(String caseId) {
        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(CMR_RE_LISTING, caseId, CASE_TYPE_ASYLUM);

        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        log.info("Sending `{}` event for case ID `{}`", CMR_RE_LISTING, caseId);
        coreCaseDataService.triggerSubmitEvent(CMR_RE_LISTING, caseId, startEventResponse, asylumCase);
    }

    private boolean isCmrListedHearing(ServiceData serviceData) {
        return isCaseManagementReview(serviceData)
               && isListAssistCaseStatus(serviceData, ListAssistCaseStatus.LISTED);
    }

    private boolean isInitialListing(String hearingId, List<PartiesNotifiedResponse> partiesNotifiedResponses) {

        log.info("partiesNotifiedResponses for hearing " + hearingId + " : "
                 + partiesNotifiedResponses.toString());

        return partiesNotifiedResponses.isEmpty();
    }

    private boolean isCmrUpdated(
        ServiceData serviceData, List<PartiesNotifiedResponse> partiesNotifiedResponses) {
        Set<ServiceDataFieldDefinition> updatedTargetFields = findUpdatedServiceDataFields(
            serviceData, partiesNotifiedResponses, Set.of(
                NEXT_HEARING_DATE,
                HEARING_CHANNELS,
                DURATION,
                HEARING_VENUE_ID
            ));

        return !updatedTargetFields.isEmpty();
    }

    protected void updateCmrHearingDetails(
        ServiceData serviceData,
        AsylumCase asylumCase,
        boolean isAppealsLocationRefDataEnabled,
        String caseId,
        List<CourtVenue> courtVenues,
        DynamicList hearingLocationList
    ) {
        List<HearingChannel> hearingChannels = getHearingChannels(serviceData);
        String hearingVenueId = getHearingVenueId(serviceData);

        String newHearingDateTime = formatHearingDateTime(getAsylumHearingDatetime(serviceData, hearingVenueId));
        HearingCentre newHearingCentre = HandlerUtils.getLocation(hearingChannels, hearingVenueId);
        DynamicList newHearingChannel = buildHearingChannelDynmicList(hearingChannels);

        //asylumCase.write(ARIA_LISTING_REFERENCE, getListingReference());
        asylumCase.write(CMR_HEARING_DATE, newHearingDateTime);
        asylumCase.write(CMR_HEARING_LENGTH, new HoursMinutes(getHearingDuration(serviceData)));
        asylumCase.write(CMR_HEARING_CENTRE, newHearingCentre);
        asylumCase.write(CMR_HEARING_CHANNEL, newHearingChannel);

        String newHearingId = getHearingId(serviceData);
        log.info(
            "Writing {} {} to asylum case {}",
            AsylumCaseFieldDefinition.CURRENT_HEARING_ID,
            newHearingId,
            caseId
        );
        asylumCase.write(AsylumCaseFieldDefinition.CURRENT_HEARING_ID, newHearingId);

        if (isAppealsLocationRefDataEnabled) {
            asylumCase.write(AsylumCaseFieldDefinition.CMR_IS_REMOTE_HEARING,
                             isRemoteHearing(serviceData) ? YES : NO);
            log.info("updateListCaseHearingDetails for Case ID `{}` serviceData contains '{}", caseId, serviceData);

            asylumCase.write(AsylumCaseFieldDefinition.CMR_HEARING_CENTRE_ADDRESS,
                             new DynamicList(
                                 new Value(getHearingVenueId(serviceData),
                                           getHearingCourtName(serviceData, courtVenues)),
                                 hearingLocationList.getListItems()
                             )
            );

            log.info("updateListCaseHearingDetails for Case ID `{}` listingLocation contains '{}'", caseId,
                     asylumCase.read(AsylumCaseFieldDefinition.LISTING_LOCATION).toString());
        }
    }
}
