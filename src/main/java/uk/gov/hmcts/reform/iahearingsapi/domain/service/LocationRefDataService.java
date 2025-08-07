package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.refdata.LocationRefDataApi;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationRefDataService {

    private static final String OPEN = "Open";
    private static final String Y = "Y";

    private final AuthTokenGenerator authTokenGenerator;
    private final UserDetails userDetails;
    private final LocationRefDataApi locationRefDataApi;
    private final IdamService idamService;
    @org.springframework.beans.factory.annotation.Value("${ia.hmctsServiceId}")
    private String serviceId;

    public DynamicList getHearingLocationsDynamicList(boolean isServiceUser) {

        List<CourtVenue> courtVenues = isServiceUser ? getCourtVenuesAsServiceUser() : getCourtVenues();

        return new DynamicList(new Value("", ""), courtVenues.stream()
            .filter(this::isOpenHearingLocation)
            .map(courtVenue -> new Value(courtVenue.getEpimmsId(), courtVenue.getCourtName()))
            .toList());
    }

    private List<CourtVenue> getCourtVenues() {

        CourtLocationCategory locationCategory = locationRefDataApi
            .getCourtVenues(userDetails.getAccessToken(), authTokenGenerator.generate(), serviceId);

        return locationCategory == null
            ? Collections.emptyList()
            : locationCategory.getCourtVenues();
    }

    public List<CourtVenue> getCourtVenuesAsServiceUser() {

        CourtLocationCategory locationCategory = locationRefDataApi
            .getCourtVenues(idamService.getServiceUserToken(), authTokenGenerator.generate(), serviceId);

        return locationCategory == null
            ? Collections.emptyList()
            : locationCategory.getCourtVenues();
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }


    private boolean isOpenHearingLocation(CourtVenue courtVenue) {
        return Objects.equals(courtVenue.getCourtStatus(), OPEN)
               && Objects.equals(courtVenue.getIsHearingLocation(), Y);
    }
}
