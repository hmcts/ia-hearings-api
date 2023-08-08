package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UnavailabilityRangeModel {

    private String unavailableFromDate;
    private String unavailableToDate;
    private UnavailabilityType unavailabilityType;
}
