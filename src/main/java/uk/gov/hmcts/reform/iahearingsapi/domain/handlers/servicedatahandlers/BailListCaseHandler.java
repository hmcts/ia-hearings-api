package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static java.util.Objects.requireNonNull;

import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.CURRENT_HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State.APPLICATION_SUBMITTED;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.ServiceHearingValuesProvider.BAILS_LOCATION_REF_DATA_FEATURE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.ListingEvent;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.ServiceDataResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.handlers.ServiceDataHandler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BailListCaseHandler
    extends ListedHearingService implements ServiceDataHandler<ServiceData> {

    private final CoreCaseDataService coreCaseDataService;
    private final FeatureToggler featureToggler;
    private final LocationRefDataService locationRefDataService;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(ServiceData serviceData
    ) {
        requireNonNull(serviceData, "serviceData must not be null");

        String caseId = getCaseReference(serviceData);
        State caseState = coreCaseDataService.getCaseState(caseId);

        return isBailListedHearing(serviceData) && caseState.equals(APPLICATION_SUBMITTED);
    }

    public ServiceDataResponse<ServiceData> handle(ServiceData serviceData) {
        if (!canHandle(serviceData)) {
            throw new IllegalStateException("Cannot handle service data");
        }

        String caseId = getCaseReference(serviceData);
        log.info("BailListCaseHandler called for  Case ID `{}`", caseId);

        StartEventResponse startEventResponse =
            coreCaseDataService.startCaseEvent(CASE_LISTING, caseId, CASE_TYPE_BAIL);
        BailCase bailCase = coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse);
        log.info("bailCase for  Case ID `{}` contains '{}'", caseId, bailCase.toString());

        boolean isBailsLocationRefDataEnabled = false;

        isBailsLocationRefDataEnabled = featureToggler.getValueAsServiceUser(
            BAILS_LOCATION_REF_DATA_FEATURE, false);

        log.info("isBailsLocationRefDataEnabled value is " + isBailsLocationRefDataEnabled);

        updateInitialBailCaseListing(serviceData, bailCase, isBailsLocationRefDataEnabled, caseId,
            locationRefDataService.getCourtVenuesAsServiceUser(),
            locationRefDataService.getHearingLocationsDynamicList(true));

        log.info("Sending `{}` event for  Case ID `{}`", CASE_LISTING, caseId);
        coreCaseDataService.triggerBailSubmitEvent(CASE_LISTING, caseId,
                                                   startEventResponse, bailCase);
        log.info("Completed `{}` event for  Case ID `{}`", CASE_LISTING, caseId);
        return new ServiceDataResponse<>(serviceData);
    }

    protected void updateInitialBailCaseListing(
        ServiceData serviceData,
        BailCase bailCase,
        boolean isRefDataLocationEnabled,
        String caseId,
        List<CourtVenue> courtVenues,
        DynamicList hearingLocationList
    ) {
        LocalDateTime hearingDateTime = getBailHearingDatetime(serviceData);

        bailCase.write(LISTING_EVENT, ListingEvent.INITIAL_LISTING.toString());
        bailCase.write(LISTING_HEARING_DATE, formatHearingDateTime(hearingDateTime));
        bailCase.write(LISTING_HEARING_DURATION, String.valueOf(getHearingDuration(serviceData)));
        bailCase.write(LISTING_LOCATION, getHearingCentre(serviceData).getValue());

        String newHearingId = getHearingId(serviceData);
        log.info("Writing {} {} to bail case {}", CURRENT_HEARING_ID, newHearingId, caseId);
        bailCase.write(CURRENT_HEARING_ID, newHearingId);

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
}

