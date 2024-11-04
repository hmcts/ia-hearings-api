package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.GLASGOW_TRIBUNALS_CENTRE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.HATTON_CROSS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre.REMOTE_HEARING;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.*;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.TEL;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail.ListingEvent;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response.PartiesNotifiedResponse;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;

class ListedHearingServiceTest {

    private static final String GLASGOW_EPIMMS_ID = "366559";
    public static final String LISTING_REF = "LAI";

    ServiceData serviceData;
    AsylumCase asylumCase;
    BailCase bailCase;
    ListedHearingService listedHearingService;
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
            "Open"));

    private final DynamicList hearingLocationList = new DynamicList(
        new Value("745389", "Hendon Magistrates Court"),
        List.of(new Value("745389", "Hendon Magistrates Court")));

    @BeforeEach
    public void setUp() {
        asylumCase = new AsylumCase();
        bailCase = new BailCase();
        serviceData = new ServiceData();
        listedHearingService = new ListedHearingService();
    }

    @Test
    void isSubstantiveCancelledHearing() {
        setUpForNonPaperSubstantiveHearing();
        serviceData.write(ServiceDataFieldDefinition.HMC_STATUS, HmcStatus.CANCELLED);

        assertTrue(listedHearingService.isSubstantiveCancelledHearing(serviceData));
    }

    @ParameterizedTest
    @MethodSource("updateListCaseHearingDetailsSource")
    void updateListCaseHearingDetails(String venueId, HearingChannel channel,
                                      String hearingDate, HearingCentre expectedHearingCentre,
                                      YesOrNo expectedIsRemoteHearing, boolean isRefDataLocationEnabled) {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
            List.of(channel));
        serviceData.write(ServiceDataFieldDefinition.HEARING_TYPE, SUBSTANTIVE.getKey());
        serviceData.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, hearingDate);
        serviceData.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, venueId);
        serviceData.write(DURATION, 200);
        serviceData.write(HEARING_ID, "12345");

        asylumCase.write(LIST_CASE_HEARING_DATE, hearingDate);
        asylumCase.write(LIST_CASE_HEARING_CENTRE, GLASGOW_TRIBUNALS_CENTRE);
        asylumCase.write(HEARING_CHANNEL, new DynamicList(
            new Value(HearingChannel.INTER.name(), HearingChannel.INTER.getLabel()),
            List.of(new Value(HearingChannel.INTER.name(), HearingChannel.INTER.getLabel()))));

        listedHearingService.updateListCaseHearingDetails(serviceData, asylumCase,
            isRefDataLocationEnabled, "caseId", courtVenueList, hearingLocationList);

        String refDataCourt = isRefDataLocationEnabled
            ? courtVenueList.stream().filter(c -> c.getEpimmsId().equals(venueId))
            .map(CourtVenue::getCourtName).findFirst().get()
            : expectedHearingCentre.getValue();

        DynamicList expectedRefDataListingLocation = new DynamicList(
            new Value(venueId, refDataCourt),
            hearingLocationList.getListItems());

        if (isRefDataLocationEnabled) {
            assertEquals(Optional.of(expectedIsRemoteHearing), asylumCase.read(
                AsylumCaseFieldDefinition.IS_REMOTE_HEARING));
            assertEquals(Optional.of(expectedRefDataListingLocation), asylumCase.read(
                AsylumCaseFieldDefinition.LISTING_LOCATION));
        } else {
            assertEquals(Optional.empty(), asylumCase.read(
                AsylumCaseFieldDefinition.IS_REMOTE_HEARING));
        }

        assertEquals(Optional.of(LISTING_REF), asylumCase.read(ARIA_LISTING_REFERENCE));
        assertEquals(Optional.of(hearingDate), asylumCase.read(LIST_CASE_HEARING_DATE));
        assertEquals(Optional.of(new HoursMinutes(200)), asylumCase.read(LISTING_LENGTH));
        assertEquals(Optional.of(expectedHearingCentre), asylumCase.read(LIST_CASE_HEARING_CENTRE));
        DynamicList newHearingChannel = new DynamicList(
            new Value(channel.name(), channel.getLabel()),
            List.of(new Value(channel.name(), channel.getLabel())));
        assertEquals(Optional.of(newHearingChannel), asylumCase.read(HEARING_CHANNEL));


    }

    private static Stream<Arguments> updateListCaseHearingDetailsSource() {

        return Stream.of(
            Arguments.of(HATTON_CROSS.getEpimsId(), HearingChannel.INTER,
                "2023-12-02T10:00:00.000", HATTON_CROSS, NO, false),
            Arguments.of(GLASGOW_EPIMMS_ID, TEL,
                "2023-12-02T09:45:00.000", REMOTE_HEARING, YES, true),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.INTER,
                "2023-12-02T09:45:00.000", GLASGOW_TRIBUNALS_CENTRE, NO, true),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.INTER,
                "2023-12-02T09:45:00.000", GLASGOW_TRIBUNALS_CENTRE, null, false)
        );
    }

    private void setUpForNonPaperSubstantiveHearing() {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
            List.of(HearingChannel.INTER, TEL, VID, HearingChannel.NA));
        serviceData.write(ServiceDataFieldDefinition.HEARING_TYPE, SUBSTANTIVE.getKey());
    }

    @ParameterizedTest
    @MethodSource("updateBailListCaseHearingDetailsSource")
    void updateBailListCaseHearingDetails(String venueId, HearingChannel channel,
                                          String hearingDate, HearingCentre expectedHearingCentre,
                                          YesOrNo expectedIsRemoteHearing,
                                          boolean isRefDataLocationEnabled) {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
                          List.of(channel));
        serviceData.write(ServiceDataFieldDefinition.HEARING_TYPE, SUBSTANTIVE.getKey());
        serviceData.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, hearingDate);
        serviceData.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, venueId);
        serviceData.write(DURATION, 60);

        bailCase.write(LISTING_EVENT, ListingEvent.INITIAL_LISTING.toString());
        bailCase.write(LISTING_HEARING_DATE, hearingDate);
        bailCase.write(LISTING_LOCATION, REMOTE_HEARING.getValue());

        listedHearingService.updateInitialBailCaseListing(serviceData, bailCase,
            isRefDataLocationEnabled, "caseId", courtVenueList, hearingLocationList);

        assertEquals(Optional.of(hearingDate), bailCase.read(LISTING_HEARING_DATE));
        assertEquals(Optional.of("60"), bailCase.read(LISTING_HEARING_DURATION));

        if (bailCase.read(IS_REMOTE_HEARING).equals(Optional.of(YES))) {
            assertEquals(Optional.of(REMOTE_HEARING.getValue()), bailCase.read(LISTING_LOCATION));
        } else {
            assertEquals(Optional.of(expectedHearingCentre.getValue()), bailCase.read(LISTING_LOCATION));
        }

        String refDataCourt = isRefDataLocationEnabled
            ? courtVenueList.stream().filter(c -> c.getEpimmsId().equals(venueId))
                .map(CourtVenue::getCourtName).findFirst().get()
            : expectedHearingCentre.getValue();

        DynamicList expectedRefDataListingLocation = new DynamicList(
            new Value(venueId, refDataCourt),
            hearingLocationList.getListItems());

        if (isRefDataLocationEnabled) {
            assertEquals(Optional.of(expectedIsRemoteHearing), bailCase.read(IS_REMOTE_HEARING));
            assertEquals(Optional.of(expectedRefDataListingLocation), bailCase.read(REF_DATA_LISTING_LOCATION));
        } else {
            assertEquals(Optional.empty(), bailCase.read(IS_REMOTE_HEARING));
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


    @ParameterizedTest
    @MethodSource("updateBailListCaseHearingDetailsSource")
    void update_relisting_bail_case_listing(String venueId, HearingChannel channel,
                                            String hearingDate, HearingCentre expectedHearingCentre,
                                            YesOrNo expectedIsRemoteHearing,
                                            boolean isRefDataLocationEnabled) {
        ServiceData serviceData = mock(ServiceData.class);
        LocalDateTime nextHearingDate = mock(LocalDateTime.class);

        when(serviceData.read(NEXT_HEARING_DATE, LocalDateTime.class))
            .thenReturn(Optional.of(nextHearingDate));
        when(nextHearingDate.format(any(DateTimeFormatter.class))).thenReturn(hearingDate);
        when(serviceData.read(HEARING_CHANNELS)).thenReturn(Optional.of(List.of(channel)));
        when(serviceData.read(HEARING_VENUE_ID, String.class)).thenReturn(Optional.of(venueId));
        when(serviceData.read(DURATION, Integer.class)).thenReturn(Optional.of(60));
        BailCase bailCase = mock(BailCase.class);
        Set<ServiceDataFieldDefinition> fieldsToUpdate =
            Set.of(NEXT_HEARING_DATE, HEARING_CHANNELS, HEARING_VENUE_ID, DURATION);

        listedHearingService.updateRelistingBailCaseListing(serviceData, bailCase,
            fieldsToUpdate, isRefDataLocationEnabled, courtVenueList, hearingLocationList);

        String refDataCourt = isRefDataLocationEnabled
            ? courtVenueList.stream().filter(c -> c.getEpimmsId().equals(venueId))
                .map(CourtVenue::getCourtName).findFirst().get()
            : venueId;

        DynamicList expectedRefDataListingLocation = new DynamicList(
            new Value(venueId, refDataCourt),
            hearingLocationList.getListItems());

        verify(bailCase).write(LISTING_HEARING_DATE, hearingDate);
        verify(bailCase).write(LISTING_HEARING_DURATION, "60");
        verify(bailCase).write(LISTING_EVENT, ListingEvent.RELISTING.toString());
        verify(bailCase).write(LISTING_LOCATION, expectedHearingCentre.getValue());
        if (isRefDataLocationEnabled) {
            verify(bailCase).write(IS_REMOTE_HEARING, expectedIsRemoteHearing);
            verify(bailCase).write(REF_DATA_LISTING_LOCATION, expectedRefDataListingLocation);
        } else {
            verify(bailCase, never()).write(IS_REMOTE_HEARING, expectedIsRemoteHearing);
        }

    }

    private static Stream<Arguments> findUpdatedServiceDataFieldsSource() {

        return Stream.of(
            Arguments.of(HATTON_CROSS.getEpimsId(), VID, "2023-12-02T10:00:00.000", 120, true),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.INTER, "2024-12-06T10:00:00.000", 60, false),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.INTER, "2024-12-06T10:00:00.000", 90, true),
            Arguments.of(HATTON_CROSS.getEpimsId(), HearingChannel.INTER, "2023-12-02T09:45:00.000", 30, true),
            Arguments.of(HATTON_CROSS.getEpimsId(), VID, "2024-12-02T10:00:00.000", 150, true),
            Arguments.of(HATTON_CROSS.getEpimsId(), TEL, "2023-12-02T09:45:00.000", 60, true)
        );
    }

    @ParameterizedTest
    @MethodSource("findUpdatedServiceDataFieldsSource")
    void test_findUpdatedServiceDataFields(String venueId, HearingChannel hearingChannel, String hearingDate,
                                           int duration, boolean expected) {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS, List.of(HearingChannel.INTER));
        serviceData.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, "2024-12-06T10:00:00.000");
        serviceData.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, GLASGOW_EPIMMS_ID);
        serviceData.write(DURATION, 60);

        ServiceData previousServiceData1 = new ServiceData();
        previousServiceData1.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
                          List.of(hearingChannel));
        previousServiceData1.write(ServiceDataFieldDefinition.NEXT_HEARING_DATE, hearingDate);
        previousServiceData1.write(ServiceDataFieldDefinition.HEARING_VENUE_ID, venueId);
        previousServiceData1.write(DURATION, duration);

        List<PartiesNotifiedResponse> partiesNotifiedResponses = List.of(
            PartiesNotifiedResponse.builder().serviceData(previousServiceData1)
                .responseReceivedDateTime(LocalDateTime.parse(hearingDate)).build(),
            PartiesNotifiedResponse.builder()
                .serviceData(serviceData)
                .responseReceivedDateTime(LocalDateTime.parse("2023-05-09T12:20:15.100")).build()
        );

        Set<ServiceDataFieldDefinition> targetFields = Set.of(NEXT_HEARING_DATE,
                                                              HEARING_CHANNELS,
                                                              DURATION,
                                                              HEARING_VENUE_ID);
        boolean actual = listedHearingService.findUpdatedServiceDataFields(
            serviceData, partiesNotifiedResponses, targetFields).size() > 0;

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({"Telephone", "Video"})
    void isRemoteHearing_true(String hearingChannel) {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
            List.of(HearingChannel.from(hearingChannel).get()));

        assertTrue(listedHearingService.isRemoteHearing(serviceData));
    }

    @ParameterizedTest
    @CsvSource({"In Person", "Not in Attendance", "On the Papers"})
    void isRemoteHearing_false(String hearingChannel) {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
            List.of(HearingChannel.from(hearingChannel).get()));

        assertFalse(listedHearingService.isRemoteHearing(serviceData));
    }

    @Test
    void getHearingCourtName() {
        List<CourtVenue> courtVenueList = List.of(new CourtVenue("Manchester Magistrates",
            "Manchester Magistrates Court",
            "231596",
            "Y",
            "Open"));

        serviceData.write(HEARING_VENUE_ID, "231596");

        assertEquals("Manchester Magistrates Court",
            listedHearingService.getHearingCourtName(serviceData, courtVenueList));
    }

    @Test
    void getHearingCourtName_exception() {
        List<CourtVenue> courtVenueList = List.of(new CourtVenue("Manchester Magistrates",
            "Manchester Magistrates Court",
            "231596",
            "Y",
            "Open"));

        serviceData.write(HEARING_VENUE_ID, "unmatchedId");

        NoSuchElementException thrown = assertThrows(
            NoSuchElementException.class,
            () -> listedHearingService.getHearingCourtName(serviceData, courtVenueList)
        );

        assertEquals("No matching ref data court venue found for epims id unmatchedId", thrown.getMessage());
    }
}
