package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingRequestDetails {

    @JsonProperty("hearingRequestID")
    private String hearingRequestId;
    private String status;
    private LocalDateTime timestamp;
    private Long versionNumber;
    private String hearingGroupRequestId;
    private LocalDateTime partiesNotified;
}