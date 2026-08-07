package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.refdata.LocationRefDataApi;

@ExtendWith(MockitoExtension.class)
public class LocationRefDataServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private UserDetails userDetails;

    @Mock
    private LocationRefDataApi locationRefDataApi;

    @Mock
    CourtLocationCategory locationCategory;

    @Mock
    CourtVenue openHearingCourtVenue;

    @Mock
    CourtVenue closedHearingCourtVenue;

    @Mock
    CourtVenue openNonHearingCourtVenue;

    @Mock
    CourtVenue closedNonHearingCourtVenue;

    @Mock
    DynamicList dynamicList;

    @Mock
    IdamService idamService;

    private LocationRefDataService locationRefDataService;

    private static final String COURT_ADDRESS = "Crown Square";
    private static final String POSTCODE = "M60 9DJ";
    private static final String GLASGOW_EPIMS_ID = "366559";

    private final String serviceId = "BFA1";
    private String authToken = "authToken";
    private String serviceUserToken = "serviceUserToken";

    @BeforeEach
    void setup() {
        locationRefDataService = new LocationRefDataService(
            authTokenGenerator,
            userDetails,
            locationRefDataApi,
            idamService,
            serviceId
        );
    }

    @Test
    void should_return_dynamicList_when_getHearingLocationsDynamicList() {
        String token = "token";
        when(userDetails.getAccessToken()).thenReturn(token);
        when(authTokenGenerator.generate()).thenReturn(authToken);
        when(locationRefDataApi.getCourtVenues(
            token,
            authToken,
            serviceId
        )).thenReturn(locationCategory);

        openHearingCourtVenue = new CourtVenue("Manchester Magistrates",
                                               "Manchester Magistrates Court",
                                               "783803",
                                               "Y",
                                               "Open",
                                               COURT_ADDRESS,
                                               POSTCODE,
                                               "Court");

        closedHearingCourtVenue = new CourtVenue("Manchester Magistrates",
                                               "Manchester Magistrates Court",
                                               "783803",
                                               "Y",
                                               "Closed",
                                               COURT_ADDRESS,
                                               POSTCODE,
                                               "Court");

        openNonHearingCourtVenue = new CourtVenue("Manchester Magistrates",
                                               "Manchester Magistrates Court",
                                               "783803",
                                               "N",
                                               "Open",
                                               COURT_ADDRESS,
                                               POSTCODE,
                                               "Court");

        closedNonHearingCourtVenue = new CourtVenue("Manchester Magistrates",
                                                 "Manchester Magistrates Court",
                                                 "783803",
                                                 "N",
                                                 "Closed",
                                                 COURT_ADDRESS,
                                                 POSTCODE,
                                                 "Court");

        when(locationCategory.getCourtVenues()).thenReturn(List.of(
            openHearingCourtVenue,
            openNonHearingCourtVenue,
            closedHearingCourtVenue,
            closedNonHearingCourtVenue)
        );
        dynamicList = new DynamicList(new Value("", ""),
                                      List.of(new Value(openHearingCourtVenue.getEpimmsId(),
                                                        openHearingCourtVenue.getCourtName())));

        assertEquals(dynamicList, locationRefDataService.getHearingLocationsDynamicList(false));
    }

    @Test
    void getCourtVenuesAsServiceUser() {
        when(idamService.getServiceUserToken()).thenReturn(serviceUserToken);
        when(authTokenGenerator.generate()).thenReturn(authToken);
        when(locationRefDataApi.getCourtVenues(
            serviceUserToken,
            authToken,
            serviceId
        )).thenReturn(locationCategory);

        List<CourtVenue> courtVenueList = List.of(new CourtVenue("Manchester Magistrates",
            "Manchester Magistrates Court",
            "783803",
            "Y",
            "Open",
            COURT_ADDRESS,
            POSTCODE,
            "Court"));

        when(locationCategory.getCourtVenues()).thenReturn(courtVenueList);

        assertEquals(courtVenueList, locationRefDataService.getCourtVenuesAsServiceUser());
    }

    @Test
    void should_return_assembled_address_for_matching_epims_id() {
        mockCourtVenues(List.of(glasgowCourtVenue()));

        assertEquals("Glasgow Tribunals Centre, Atlantic Quay, 20 York Street, G2 8GT",
                     locationRefDataService.getHearingCentreAddress(GLASGOW_EPIMS_ID));
    }

    @Test
    void should_return_empty_address_when_no_court_venue_matches_epims_id() {
        mockCourtVenues(List.of(glasgowCourtVenue()));

        assertEquals("", locationRefDataService.getHearingCentreAddress("unmatchedEpimsId"));
    }

    @Test
    void should_return_empty_address_when_no_court_venues_returned() {
        when(userDetails.getAccessToken()).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(authToken);
        when(locationRefDataApi.getCourtVenues(authToken, authToken, serviceId)).thenReturn(null);

        assertEquals("", locationRefDataService.getHearingCentreAddress(GLASGOW_EPIMS_ID));
    }

    @Test
    void should_skip_null_address_parts_when_assembling_address() {
        mockCourtVenues(List.of(new CourtVenue("Glasgow Tribunals Centre",
                                               null,
                                               GLASGOW_EPIMS_ID,
                                               "Y",
                                               "Open",
                                               null,
                                               null,
                                               "Court")));

        assertEquals(", , ", locationRefDataService.getHearingCentreAddress(GLASGOW_EPIMS_ID));
    }

    @Test
    void should_return_address_for_hearing_centre() {
        mockCourtVenues(List.of(glasgowCourtVenue()));

        assertEquals("Glasgow Tribunals Centre, Atlantic Quay, 20 York Street, G2 8GT",
                     locationRefDataService.getHearingCentreAddress(HearingCentre.GLASGOW_TRIBUNALS_CENTRE));
    }

    @Test
    void should_return_remote_hearing_for_remote_hearing_centre() {
        assertEquals("Remote hearing", locationRefDataService.getHearingCentreAddress(HearingCentre.REMOTE_HEARING));
    }

    @Test
    void should_look_up_address_for_national_virtual_hearing_centre() {
        mockCourtVenues(List.of(new CourtVenue("IAC National Virtual Region",
                                               "IAC National Virtual Region",
                                               HearingCentre.IAC_NATIONAL_VIRTUAL.getEpimsId(),
                                               "Y",
                                               "Open",
                                               "Remote",
                                               "",
                                               "Court")));

        assertEquals("IAC National Virtual Region, Remote, ",
                     locationRefDataService.getHearingCentreAddress(HearingCentre.IAC_NATIONAL_VIRTUAL));
    }

    private void mockCourtVenues(List<CourtVenue> courtVenues) {
        when(userDetails.getAccessToken()).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(authToken);
        when(locationRefDataApi.getCourtVenues(authToken, authToken, serviceId)).thenReturn(locationCategory);
        when(locationCategory.getCourtVenues()).thenReturn(courtVenues);
    }

    private CourtVenue glasgowCourtVenue() {
        return new CourtVenue("Glasgow Tribunals Centre",
                              "Glasgow Tribunals Centre",
                              GLASGOW_EPIMS_ID,
                              "Y",
                              "Open",
                              "Atlantic Quay, 20 York Street",
                              "G2 8GT",
                              "Court");
    }
}
