package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class HearingDay {

    private LocalDateTime hearingStartDateTime;
    private LocalDateTime hearingEndDateTime;
}
