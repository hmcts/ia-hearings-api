package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UnavailabilityDayOfWeekModel {

    @JsonProperty("DOW")
    private DoW dayOfWeek;
    @JsonProperty("DOWUnavailabilityType")
    private UnavailabilityType dayOfWeekUnavailabilityType;
}
