package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityRangeModel {

    private String unavailableFromDate;
    private String unavailableToDate;
    private UnavailabilityType unavailabilityType;
}
