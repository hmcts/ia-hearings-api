package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.*;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.ListingEvent.INITIAL_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.COSTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.ServiceHearingValuesProvider.BAILS_LOCATION_REF_DATA_FEATURE;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BailListCaseHandlerTest {

    private static final String CASE_REF = "1111";
    private static final LocalDateTime NEXT_HEARING_DATE = LocalDateTime.of(2023, 9, 29, 12, 0);
    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    ServiceData serviceData;
    @Mock
    StartEventResponse startEventResponse;
    @Mock
    BailCase bailCase;
    @Mock
    FeatureToggler featureToggler;
    @Mock
    LocationRefDataService locationRefDataService;

    private BailListCaseHandler bailListCaseHandler;

    private DynamicList hearingLocationList = new DynamicList(
        new Value("745389", "Hendon Magistrates Court"),
        List.of(new Value("745389", "Hendon Magistrates Court")));

    @BeforeEach
    public void setUp() {

        bailListCaseHandler =
            new BailListCaseHandler(coreCaseDataService, featureToggler, locationRefDataService);

        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.LISTED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.FIXED));
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(BAIL.getKey()));
        when(coreCaseDataService.getCaseState(CASE_REF)).thenReturn(State.APPLICATION_SUBMITTED);
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(featureToggler.getValueAsServiceUser(BAILS_LOCATION_REF_DATA_FEATURE, false)).thenReturn(false);

        List<CourtVenue> courtVenueList = List.of(new CourtVenue("Manchester Magistrates",
            "Manchester Magistrates Court",
            "231596",
            "Y",
            "Open"));
        when(locationRefDataService.getCourtVenuesAsServiceUser()).thenReturn(courtVenueList);
        when(locationRefDataService.getHearingLocationsDynamicList(true)).thenReturn(hearingLocationList);

    }

    @Test
    void should_have_early_dispatch_priority() {
        Assertions.assertEquals(DispatchPriority.EARLY, bailListCaseHandler.getDispatchPriority());
    }

    @Test
    void should_handle_only_if_service_data_qualifies() {
        Assertions.assertTrue(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_type_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_TYPE, String.class))
            .thenReturn(Optional.of(COSTS.getKey()));
        Assertions.assertFalse(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hmc_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.class))
            .thenReturn(Optional.of(HmcStatus.CLOSED));
        Assertions.assertFalse(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_channels_on_papers() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS, List.class))
            .thenReturn(Optional.of(List.of(HearingChannel.ONPPRS)));
        Assertions.assertFalse(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_not_handle_if_hearing_listing_status_unqualified() {
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_LISTING_STATUS, ListingStatus.class))
            .thenReturn(Optional.of(ListingStatus.DRAFT));
        Assertions.assertFalse(bailListCaseHandler.canHandle(serviceData));
    }

    @Test
    void should_trigger_case_listing() {
        when(serviceData.read(ServiceDataFieldDefinition.CASE_REF, String.class)).thenReturn(Optional.of(CASE_REF));
        when(coreCaseDataService.startCaseEvent(CASE_LISTING, CASE_REF, CASE_TYPE_BAIL))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getBailCaseFromStartedEvent(startEventResponse)).thenReturn(bailCase);
        when(serviceData.read(ServiceDataFieldDefinition.HEARING_CHANNELS))
            .thenReturn(Optional.of(List.of(HearingChannel.VID)));
        when(serviceData.read(ServiceDataFieldDefinition.NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(NEXT_HEARING_DATE));
        when(serviceData.read(DURATION, Integer.class))
            .thenReturn(Optional.of(60));
        when(serviceData.read(HEARING_VENUE_ID, String.class))
            .thenReturn(Optional.of("231596"));
        when(serviceData.read(HEARING_ID, String.class))
            .thenReturn(Optional.of("12345"));

        bailListCaseHandler.handle(serviceData);

        verify(bailCase).write(LISTING_EVENT, INITIAL_LISTING.toString());
        verify(bailCase).write(LISTING_HEARING_DURATION, "60");
        verify(bailCase).write(LISTING_HEARING_DATE,
                               LocalDateTime.of(2023, 9, 29, 12, 0)
                                   .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
        verify(bailCase).write(LISTING_LOCATION, HearingCentre.REMOTE_HEARING.getValue());

        verify(coreCaseDataService).triggerBailSubmitEvent(CASE_LISTING, CASE_REF,
                                                           startEventResponse, bailCase);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(coreCaseDataService.getCaseState(CASE_REF)).thenReturn(State.APPEAL_STARTED);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> bailListCaseHandler.handle(serviceData))
            .hasMessage("Cannot handle service data")
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}
