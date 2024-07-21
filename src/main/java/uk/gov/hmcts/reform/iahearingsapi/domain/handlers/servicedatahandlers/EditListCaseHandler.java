package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.FINAL_BUNDLING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.PRE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers.HandlerUtils.getHearingDateAndTime;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_ASYLUM;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EditListCaseHandler extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;


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

        if (tryUpdateListCaseHearingDetails(asylumCase, serviceData)) {
            log.info("Sending `{}` event for case ID `{}`", EDIT_CASE_LISTING, caseId);
            coreCaseDataService.triggerSubmitEvent(EDIT_CASE_LISTING, caseId, startEventResponse, asylumCase);
        }

        return new ServiceDataResponse<>(serviceData);
    }

    private boolean tryUpdateListCaseHearingDetails(AsylumCase asylumCase, ServiceData serviceData) {

        final List<HearingChannel> nextHearingChannelList = getHearingChannels(serviceData);

        final LocalDateTime nextHearingDateTime = serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("nextHearingDate can not be null"));

        final LocalDateTime currentHearingDateTime = LocalDateTime.parse(asylumCase.read(
            LIST_CASE_HEARING_DATE,
            String.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingDate can not be null")));

        final String currentVenueId = asylumCase.read(
            LIST_CASE_HEARING_CENTRE,
            HearingCentre.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingCentre can not be null")).getEpimsId();


        final String currentDuration = asylumCase.read(
            LIST_CASE_HEARING_LENGTH,
            String.class
        ).orElseThrow(() -> new IllegalStateException("listCaseHearingLength can not be null"));

        final String nextHearingChannel = nextHearingChannelList.get(0).name();

        final boolean isRemoteHearing = nextHearingChannel.equals(VID.name()) || nextHearingChannel.equals(TEL.name());

        // the nextHearingDateTime has to be recalculated according to the actual physical venue (Glasgow / non-Glasgow)
        final String physicalNextHearingVenueId = getHearingVenueId(serviceData);
        final LocalDateTime calculatedNextHearingDateTime = getHearingDateAndTime(nextHearingDateTime,
                                                                                  physicalNextHearingVenueId);

        final boolean hearingDateTimeChanged = !(currentHearingDateTime).equals(calculatedNextHearingDateTime);

        // venue id shown on frontend and in notifications will show as Remote Hearing if it's remote
        final String nextHearingVenueId = isRemoteHearing
            ? REMOTE_HEARING.getEpimsId()
            : getHearingVenueId(serviceData);

        boolean hearingChannelChanged;

        Optional<DynamicList> currentHearingChannels = asylumCase.read(HEARING_CHANNEL, DynamicList.class);

        if (currentHearingChannels.isEmpty()) {
            hearingChannelChanged = false;
        } else {
            hearingChannelChanged = !currentHearingChannels.get().getValue().toString().equals(nextHearingChannel);
        }

        final boolean hearingVenueChanged = !currentVenueId.equals(nextHearingVenueId);
        final boolean hearingCentreChanged = hearingChannelChanged || hearingVenueChanged;
        final int nextDuration = getHearingDuration(serviceData);
        final boolean durationChanged = !currentDuration.equals(String.valueOf(nextDuration));
        boolean sendUpdate = false;

        String hearingId = serviceData.read(HEARING_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("hearing id can not be null"));

        // listCaseHearingDate has to be recalculated based on the hearing venue
        if (hearingDateTimeChanged) {
            asylumCase.write(
                LIST_CASE_HEARING_DATE,
                calculatedNextHearingDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"))
            );
            sendUpdate = true;
            log.info("hearing date updated for hearing " + hearingId);
        }
        if (durationChanged) {
            asylumCase.write(LIST_CASE_HEARING_LENGTH, String.valueOf(nextDuration));
            sendUpdate = true;
            log.info("hearing length updated for hearing " + hearingId);
        }
        if (hearingChannelChanged) {
            asylumCase.write(
                HEARING_CHANNEL,
                buildHearingChannelDynmicList(nextHearingChannelList));
            sendUpdate = true;
            log.info("hearing channel updated for hearing " + hearingId);
        }
        if (hearingCentreChanged) {
            asylumCase.write(
                LIST_CASE_HEARING_CENTRE,
                HandlerUtils.getLocation(nextHearingChannelList, nextHearingVenueId)
            );
            sendUpdate = true;
            log.info("hearing centre updated for hearing " + hearingId);
        }

        // Only trigger review interpreter task if the hearing location, date or channel are updated.
        if (hearingChannelChanged || hearingCentreChanged || hearingDateTimeChanged) {
            asylumCase.write(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YES);
            log.info("Setting trigger review interpreter task flag for hearing " + hearingId);
        } else if (sendUpdate) {
            asylumCase.clear(SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK);
        }

        return sendUpdate;
    }
}

