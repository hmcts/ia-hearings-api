package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.StringUtils;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.TRIGGER_CMR_UPDATED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.FINAL_BUNDLING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.PREPARE_FOR_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.PRE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.INTER;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EditListCaseHandler extends SubstantiveListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private static final String GLASGOW_EPIMMS_ID = "366559";


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

        return (isSubstantiveListedHearing(serviceData)
            || isCaseManagementReviewListedHearing(serviceData))
            && targetStates.contains(caseState);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);

        StartEventResponse startEventResponse = coreCaseDataService.startCaseEvent(EDIT_CASE_LISTING, caseId);
        AsylumCase asylumCase = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);

        sendEditListingEventIfHearingIsUpdated(
            startEventResponse,
            asylumCase,
            caseId,
            serviceData
        );
        return new ServiceDataResponse<>(serviceData);
    }

    private void sendEditListingEventIfHearingIsUpdated(StartEventResponse startEventResponse,
                                                        AsylumCase asylumCase,
                                                        String caseId,
                                                        ServiceData serviceData
    ) {

        List<HearingChannel> nextHearingChannelList = getHearingChannels(serviceData);

        LocalDateTime nextHearingDate = serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class)
            .orElseThrow(() -> new IllegalStateException("nextHearingDate can not be null"));

        String nextHearingVenueId = getHearingVenueId(serviceData);

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

        if (nextHearingChannel.equals(INTER.name())) {
            nextHearingDate = StringUtils.equals(nextHearingVenueId, GLASGOW_EPIMMS_ID)
                ? nextHearingDate.with(LocalTime.of(9, 45))
                : nextHearingDate.with(LocalTime.of(10, 0));
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
        int nextDuration = getHearingDuration(serviceData);
        if ((nextHearingChannel.equals(VID.name()) || nextHearingChannel.equals(TEL.name()))
            && !currentDuration.equals(String.valueOf(nextDuration))) {
            asylumCase.write(LIST_CASE_HEARING_LENGTH, String.valueOf(nextDuration));
            sendUpdate = true;
        }

        if (sendUpdate) {
            log.info("Sending `{}` event for case ID `{}`", EDIT_CASE_LISTING, caseId);
            coreCaseDataService.triggerSubmitEvent(EDIT_CASE_LISTING, caseId, startEventResponse, asylumCase);
            if (isCaseManagementReviewListedHearing(serviceData)) {
                triggerCmrUpdatedNotification(startEventResponse, caseId);
            }
        }
    }

    private void triggerCmrUpdatedNotification(StartEventResponse startEventResponse, String caseId) {
        StartEventResponse startCmrEventResponse = coreCaseDataService.startCaseEvent(
            EDIT_CASE_LISTING,
            caseId
        );
        AsylumCase asylumCaseCmr = coreCaseDataService.getCaseFromStartedEvent(startEventResponse);
        log.info("Sending `{}` event for case ID `{}`", TRIGGER_CMR_UPDATED, caseId);
        coreCaseDataService.triggerSubmitEvent(
            TRIGGER_CMR_UPDATED,
            caseId,
            startCmrEventResponse,
            asylumCaseCmr
        );
    }
}

