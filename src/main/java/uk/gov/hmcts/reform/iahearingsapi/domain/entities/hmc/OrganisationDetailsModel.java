package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganisationDetailsModel {

    private String name;
    private String organisationType;
    private String cftOrganisationID;
}