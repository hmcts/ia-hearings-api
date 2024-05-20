package uk.gov.hmcts.reform.iahearingsapi.domain.handlers.servicedatahandlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.DURATION;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_CHANNELS;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.HEARING_VENUE_ID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceDataFieldDefinition.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingChannel.VID;
import static uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingType.SUBSTANTIVE;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
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
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;

class ListedHearingServiceTest {

    private static final String GLASGOW_EPIMMS_ID = "366559";
    public static final String LISTING_REF = "LAI";

    @Mock
    private LocalDateTime nextHearingDate;

    ServiceData serviceData;
    AsylumCase asylumCase;
    BailCase bailCase;
    ListedHearingService listedHearingService;
    private List<CourtVenue> courtVenueList = List.of(
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

    private DynamicList hearingLocationList = new DynamicList(
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
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.TEL,
                "2023-12-02T09:45:00.000", REMOTE_HEARING, YES, true),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.INTER,
                "2023-12-02T09:45:00.000", GLASGOW_TRIBUNALS_CENTRE, NO, true),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.INTER,
                "2023-12-02T09:45:00.000", GLASGOW_TRIBUNALS_CENTRE, null, false)
        );
    }

    private void setUpForNonPaperSubstantiveHearing() {
        serviceData.write(ServiceDataFieldDefinition.HEARING_CHANNELS,
            List.of(HearingChannel.INTER, HearingChannel.TEL, VID, HearingChannel.NA));
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
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.TEL,
                "2023-12-02T09:45:00.000", REMOTE_HEARING, YES, true),
            Arguments.of(GLASGOW_EPIMMS_ID, HearingChannel.TEL,
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
}
