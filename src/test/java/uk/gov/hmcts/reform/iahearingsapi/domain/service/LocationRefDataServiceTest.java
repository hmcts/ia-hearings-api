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
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.refdata.LocationRefDataApi;

@ExtendWith(MockitoExtension.class)
public class LocationRefDataServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

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

    private LocationRefDataService locationRefDataService;

    private final String serviceId = "BFA1";

    @BeforeEach
    void setup() {
        locationRefDataService = new LocationRefDataService(
            authTokenGenerator,
            idamService,
            locationRefDataApi
        );
        locationRefDataService.setServiceId(serviceId);
    }

    @Test
    void should_return_dynamicList_when_getHearingLocationsDynamicList() {
        String token = "token";
        when(idamService.getServiceUserToken()).thenReturn(token);
        String authToken = "authToken";
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
                                               "The Court House, Minshull Street",
                                               "M1 3FS");

        closedHearingCourtVenue = new CourtVenue("Manchester Magistrates",
                                                 "Manchester Magistrates Court",
                                                 "783803",
                                                 "Y",
                                                 "Closed",
                                                 "The Court House, Minshull Street",
                                                 "M1 3FS");

        openNonHearingCourtVenue = new CourtVenue("Manchester Magistrates",
                                                  "Manchester Magistrates Court",
                                                  "783803",
                                                  "N",
                                                  "Open",
                                                  "The Court House, Minshull Street",
                                                  "M1 3FS");

        closedNonHearingCourtVenue = new CourtVenue("Manchester Magistrates",
                                                    "Manchester Magistrates Court",
                                                    "783803",
                                                    "N",
                                                    "Closed",
                                                    "The Court House, Minshull Street",
                                                    "M1 3FS");

        when(locationCategory.getCourtVenues()).thenReturn(List.of(
            openHearingCourtVenue,
            openNonHearingCourtVenue,
            closedHearingCourtVenue,
            closedNonHearingCourtVenue)
        );
        dynamicList = new DynamicList(new Value("", ""),
                                      List.of(new Value(openHearingCourtVenue.getEpimmsId(),
                                                        openHearingCourtVenue.getCourtName())));

        assertEquals(dynamicList, locationRefDataService.getHearingLocationsDynamicList());
    }
}
