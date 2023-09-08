package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartiesNotifiedResponse {

    private LocalDateTime responseReceivedDateTime;

    private Integer requestVersion;

    private LocalDateTime partiesNotified;

    private PartiesNotifiedServiceData serviceData;
}
