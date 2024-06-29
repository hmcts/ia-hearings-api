package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.refdata.LocationRefDataApi;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationRefDataServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private UserDetails userDetails;

    @Mock
    private LocationRefDataApi locationRefDataApi;

    @Mock
    CourtLocationCategory locationCategory;

    @Mock
    CourtVenue courtVenue;

    @Mock
    DynamicList dynamicList;

    private LocationRefDataService locationRefDataService;

    private final String serviceId = "BFA1";

    @BeforeEach
    void setup() {
        locationRefDataService = new LocationRefDataService(
            authTokenGenerator,
            userDetails,
            locationRefDataApi
        );
        locationRefDataService.setServiceId(serviceId);
    }

    @Test
    void should_return_dynamicList_when_getHearingLocationsDynamicList() {
        String token = "token";
        when(userDetails.getAccessToken()).thenReturn(token);
        String authToken = "authToken";
        when(authTokenGenerator.generate()).thenReturn(authToken);
        when(locationRefDataApi.getCourtVenues(
            token,
            authToken,
            serviceId
        )).thenReturn(locationCategory);

        courtVenue = new CourtVenue("Manchester Magistrates",
                                    "Manchester Magistrates Court",
                                    "783803");
        when(locationCategory.getCourtVenues()).thenReturn(List.of(courtVenue));
        dynamicList = new DynamicList(new Value("", ""),
                                      List.of(new Value(courtVenue.getEpimmsId(), courtVenue.getCourtName())));

        assertEquals(dynamicList,
                     locationRefDataService.getHearingLocationsDynamicList());
    }
}
