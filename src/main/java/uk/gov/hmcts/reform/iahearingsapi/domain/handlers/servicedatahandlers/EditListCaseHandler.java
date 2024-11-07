package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.FINAL_BUNDLING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.PRE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.getHearingDateAndTime;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.utils.HearingsUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class EditListCaseHandler extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final LocationRefDataService locationRefDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        String caseId = getCaseReference(serviceData);
        State caseState = coreCaseDataService
            .getCaseState(caseId);
        List<State> targetStates = Arrays.asList(PREPARE_FOR_HEARING, FINAL_BUNDLING, PRE_HEARING);

        return isSubstantiveListedHearing(serviceData) && targetStates.contains(caseState);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);
        log.info("EditListCaseHandler triggered for case " + caseId);
        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(EDIT_CASE_LISTING, caseId, CASE_TYPE_ASYLUM);
        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        if (tryUpdateListCaseHearingDetails(asylumCase, serviceData, caseId)) {
            log.info("Sending `{}` event for case ID `{}`", EDIT_CASE_LISTING, caseId);
            coreCaseDataService.triggerSubmitEvent(EDIT_CASE_LISTING, caseId, startEventResponse, asylumCase);
        }

        return new ServiceDataResponse<>(serviceData);
    }

    private boolean tryUpdateListCaseHearingDetails(AsylumCase asylumCase, ServiceData serviceData, String caseId) {

        String hearingId = serviceData.read(HEARING_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("hearing id can not be null"));
        String currentHearingChannel = asylumCase.read(HEARING_CHANNEL, DynamicList.class)
            .map(dynamicList -> dynamicList.getValue().getCode()).orElse("");

        boolean hearingDateTimeUpdated = tryUpdateHearingDateTime(asylumCase, serviceData, hearingId);
        boolean hearingChannelUpdated = tryUpdateHearingChannel(asylumCase, serviceData, hearingId);
        boolean hearingDurationUpdated = tryUpdateHearingDuration(asylumCase, serviceData, hearingId);

        boolean currentChannelIsRemote = List.of(VID.name(), TEL.name()).contains(currentHearingChannel);
        boolean nextChannelIsRemote = isRemoteHearing(serviceData);
        //Channel update is not VID to TEL or TEL to VID
        boolean isNonRemoteToRemoteChannelUpdate =
            hearingChannelUpdated && !(currentChannelIsRemote && nextChannelIsRemote);
        boolean hearingLocationUpdated = tryUpdateHearingCentre(
            asylumCase, serviceData, isNonRemoteToRemoteChannelUpdate, hearingId);

        boolean sendUpdate = hearingDateTimeUpdated
                             || hearingChannelUpdated
                             || hearingLocationUpdated
                             || hearingDurationUpdated;

        // Only trigger review interpreter task if the hearing location, date or channel are updated.
        // Don not trigger when hearing channel update is remote to remote
        if (isNonRemoteToRemoteChannelUpdate || hearingLocationUpdated || hearingDateTimeUpdated) {
            asylumCase.write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
            log.info("Setting trigger review interpreter task flag for hearing " + hearingId);
        } else if (sendUpdate) {
            asylumCase.clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
        }

        assignRefDataFields(asylumCase, serviceData, caseId);

        return sendUpdate;
    }

    private boolean tryUpdateHearingDateTime(AsylumCase asylumCase, ServiceData serviceData, String hearingId) {
        final LocalDateTime nextHearingDateTime = serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("nextHearingDate can not be null"));

        final LocalDateTime currentHearingDateTime = LocalDateTime.parse(asylumCase.read(
            LIST_CASE_HEARING_DATE,
            String.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingDate can not be null")));

        // the nextHearingDateTime has to be recalculated according to the actual physical venue (Glasgow / non-Glasgow)
        final String physicalNextHearingVenueId = getHearingVenueId(serviceData);
        final LocalDateTime calculatedNextHearingDateTime = getHearingDateAndTime(nextHearingDateTime,
                                                                                  physicalNextHearingVenueId);
        boolean updated =  !currentHearingDateTime.equals(calculatedNextHearingDateTime);

        if (updated) {
            asylumCase.write(
                LIST_CASE_HEARING_DATE,
                calculatedNextHearingDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
            );
            log.info("Hearing date updated for hearing " + hearingId);
            return true;
        } else {
            log.info("Hearing date not updated for hearing " + hearingId);
            return false;
        }
    }

    private boolean tryUpdateHearingChannel(AsylumCase asylumCase, ServiceData serviceData, String hearingId) {
        final List<HearingChannel> nextHearingChannelList = getHearingChannels(serviceData);
        String currentHearingChannel = asylumCase.read(HEARING_CHANNEL, DynamicList.class)
            .map(dynamicList -> dynamicList.getValue().getCode()).orElse("");
        String nextHearingChannel = nextHearingChannelList.get(0).name();

        boolean updated = !Objects.equals(currentHearingChannel, nextHearingChannel);

        if (updated) {
            asylumCase.write(
                HEARING_CHANNEL,
                buildHearingChannelDynmicList(nextHearingChannelList));
            log.info("Hearing channel updated for hearing " + hearingId);
            return true;
        } else {
            log.info("Hearing channel not updated for hearing " + hearingId);
            return false;
        }
    }

    private boolean tryUpdateHearingCentre(
        AsylumCase asylumCase, ServiceData serviceData, boolean hearingChannelUpdated, String hearingId) {

        final String currentVenueId = asylumCase.read(
            LIST_CASE_HEARING_CENTRE,
            HearingCentre.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingCentre can not be null")).getEpimsId();
        String nextHearingVenueId = getHearingVenueId(serviceData);

        final boolean hearingVenueUpdated = !currentVenueId.equals(nextHearingVenueId);
        final boolean hearingCentreUpdated = hearingChannelUpdated || hearingVenueUpdated;

        // venue id shown on frontend and in notifications will show as Remote Hearing if it's remote
        if (isRemoteHearing(serviceData)) {
            nextHearingVenueId = REMOTE_HEARING.getEpimsId();
        }

        if (hearingCentreUpdated) {
            asylumCase.write(
                LIST_CASE_HEARING_CENTRE,
                HandlerUtils.getLocation(getHearingChannels(serviceData), nextHearingVenueId)
            );
            log.info("Hearing centre updated for hearing " + hearingId);
            return true;
        } else {
            log.info("Hearing centre not updated for hearing " + hearingId);
            return false;
        }
    }

    private boolean tryUpdateHearingDuration(AsylumCase asylumCase, ServiceData serviceData, String hearingId) {
        final int nextDuration = getHearingDuration(serviceData);
        int currentDuration = asylumCase.read(LISTING_LENGTH, HoursMinutes.class)
            .map(HoursMinutes::convertToIntegerMinutes).orElse(0);

        boolean updated = currentDuration != nextDuration;

        if (updated) {
            asylumCase.write(LISTING_LENGTH, new HoursMinutes(nextDuration));
            log.info("Hearing length updated for hearing " + hearingId);
            return true;
        } else {
            log.info("Hearing length not updated for hearing " + hearingId);
            return false;
        }
    }

    private void assignRefDataFields(AsylumCase asylumCase, ServiceData serviceData, String caseId) {

        if (!HearingsUtils.isAppealsLocationRefDataEnabled(asylumCase)) {
            return;
        }

        asylumCase.write(AsylumCaseFieldDefinition.IS_REMOTE_HEARING, isRemoteHearing(serviceData) ? YES : NO);
        log.info("tryUpdateListCaseHearingDetails for Case ID `{}` serviceData contains '{}", caseId, serviceData);

        asylumCase.write(AsylumCaseFieldDefinition.LISTING_LOCATION,
            new DynamicList(
                new Value(getHearingVenueId(serviceData),
                    getHearingCourtName(serviceData, locationRefDataService.getCourtVenuesAsServiceUser())),
                        locationRefDataService.getHearingLocationsDynamicList(true).getListItems()));

        log.info("tryUpdateListCaseHearingDetails for Case ID `{}` listingLocation contains '{}'", caseId,
                 asylumCase.read(AsylumCaseFieldDefinition.LISTING_LOCATION).toString());
    }
}

