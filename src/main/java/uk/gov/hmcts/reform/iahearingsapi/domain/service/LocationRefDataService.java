package uk.gov.hmcts.reform.iahearingsapi.domain.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtLocationCategory;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.refdata.CourtVenue;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.refdata.LocationRefDataApi;

@Service
@RequiredArgsConstructor
public class LocationRefDataService {

    private static final String OPEN = "Open";
    private static final String Y = "Y";

    private final AuthTokenGenerator authTokenGenerator;
    private final IdamService idamService;
    private final LocationRefDataApi locationRefDataApi;
    @org.springframework.beans.factory.annotation.Value("${ia.hmctsServiceId}")
    private String serviceId;

    public DynamicList getHearingLocationsDynamicList() {

        return new DynamicList(new Value("", ""), getCourtVenues().stream()
            .map(courtVenue -> new Value(courtVenue.getEpimmsId(), courtVenue.getCourtName()))
            .toList());
    }

    public List<CourtVenue> getCourtVenues() {

        CourtLocationCategory locationCategory = locationRefDataApi
            .getCourtVenues(idamService.getServiceUserToken(), authTokenGenerator.generate(), serviceId);

        return locationCategory == null
            ? Collections.emptyList()
            : locationCategory.getCourtVenues().stream().filter(this::isOpenHearingLocation).toList();
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    private boolean isOpenHearingLocation(CourtVenue courtVenue) {
        return StringUtils.equals(courtVenue.getCourtStatus(), OPEN)
               && StringUtils.equals(courtVenue.getIsHearingLocation(), Y);
    }
}
