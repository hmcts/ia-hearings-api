package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RelatedPartiesModel {

    private String relatedPartyID;
    private String relationshipType;
}
