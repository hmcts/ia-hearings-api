package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.ListingEvent;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.ListingStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iahearingsapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.GLASGOW_TRIBUNALS_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.HATTON_CROSS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.ListingEvent.INITIAL_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.Event.CASE_LISTING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.COSTS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.CoreCaseDataService.CASE_TYPE_BAIL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.service.ServiceHearingValuesProvider.BAILS_LOCATION_REF_DATA_FEATURE;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class BailListCaseHandlerTest {

    private static final String CASE_REF = "1111";
    private static final String GLASGOW_EPIMMS_ID = "366559";
    private static final LocalDateTime NEXT_HEARING_DATE = LocalDateTime.of(2023, 9, 29, 12, 0);

    private final List<CourtVenue> courtVenueList = List.of(
        new CourtVenue("Glasgow Tribunals Centre",
            "Glasgow Tribunals Centre",
            "366559",
            "Y",
            "Open"),
        new CourtVenue("Hatton Cross Tribunal Hearing Centre",
            "Hatton Cross Tribunal Hearing Centre",
            "386417",
            "Y",
            "Open")
    );

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

    private final DynamicList hearingLocationList = new DynamicList(
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

    @ParameterizedTest
    @MethodSource("updateBailListCaseHearingDetailsSource")
    void updateBailListCaseHearingDetails(String venueId, HearingChannel channel,
                                          String hearingDate, HearingCentre expectedHearingCentre,
                                          YesOrNo expectedIsRemoteHearing,
                                          boolean isRefDataLocationEnabled) {
        BailCase bailCaseParam = new BailCase();
        ServiceData serviceDataParam = new ServiceData();
        serviceDataParam.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
                List.of(channel));
        serviceDataParam.write(ServiceDataFieldDefinition.HEARING_TYPE, SUBSTANTIVE.getKey());
        serviceDataParam.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, hearingDate);
        serviceDataParam.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, venueId);
        serviceDataParam.write(DURATION, 60);
        serviceDataParam.write(HEARING_ID, "12345");

        bailCaseParam.write(LISTING_EVENT, ListingEvent.INITIAL_LISTING.toString());
        bailCaseParam.write(LISTING_HEARING_DATE, hearingDate);
        bailCaseParam.write(LISTING_LOCATION, REMOTE_HEARING.getValue());

        bailListCaseHandler.updateInitialBailCaseListing(serviceDataParam, bailCaseParam,
                isRefDataLocationEnabled, "caseId", courtVenueList, hearingLocationList);

        assertEquals(Optional.of(hearingDate), bailCaseParam.read(LISTING_HEARING_DATE));
        assertEquals(Optional.of("60"), bailCaseParam.read(LISTING_HEARING_DURATION));

        if (bailCaseParam.read(IS_REMOTE_HEARING).equals(Optional.of(YES))) {
            assertEquals(Optional.of(REMOTE_HEARING.getValue()), bailCaseParam.read(LISTING_LOCATION));
        } else {
            assertEquals(Optional.of(expectedHearingCentre.getValue()), bailCaseParam.read(LISTING_LOCATION));
        }

        String refDataCourt = isRefDataLocationEnabled
                ? courtVenueList.stream().filter(c -> c.getEpimmsId().equals(venueId))
                .map(CourtVenue::getCourtName).findFirst().get()
                : expectedHearingCentre.getValue();

        DynamicList expectedRefDataListingLocation = new DynamicList(
                new Value(venueId, refDataCourt),
                hearingLocationList.getListItems());

        if (isRefDataLocationEnabled) {
            assertEquals(Optional.of(expectedIsRemoteHearing), bailCaseParam.read(IS_REMOTE_HEARING));
            assertEquals(Optional.of(expectedRefDataListingLocation), bailCaseParam.read(REF_DATA_LISTING_LOCATION));
        } else {
            assertEquals(Optional.empty(), bailCaseParam.read(IS_REMOTE_HEARING));
        }
    }

    private static Stream<Arguments> updateBailListCaseHearingDetailsSource() {
        return Stream.of(
            Arguments.of(HATTON_CROSS.getEpimsId(), HearingChannel.INTER,
                "2023-12-02T10:00:00.000", HATTON_CROSS, NO, true),
            Arguments.of(GLASGOW_EPIMMS_ID, VID,
                "2023-12-02T09:45:00.000", REMOTE_HEARING, YES, true),
            Arguments.of(GLASGOW_EPIMMS_ID, TEL,
                "2023-12-02T09:45:00.000", REMOTE_HEARING, YES, true),
            Arguments.of(GLASGOW_EPIMMS_ID, TEL,
                "2023-12-02T09:45:00.000", REMOTE_HEARING, null, false),
            Arguments.of(GLASGOW_EPIMMS_ID, VID,
                "2023-12-02T09:45:00.000", REMOTE_HEARING, null, false),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.INTER,
                "2023-12-02T09:45:00.000", GLASGOW_TRIBUNALS_CENTRE, null, false)
        );
    }
}
