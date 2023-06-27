package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HmcHearingResponse {
    private Long hearingRequestId;
    private Long requestVersion;
    private Long responseVersion;
    private String status;
    private LocalDateTime timeStamp;
    private Long versionNumber;
}
