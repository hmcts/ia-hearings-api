package uk.gov.hmcts.reform.iahearingsapi.domain.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.model.hmc.reference.HmcStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestDetails {

    private Long versionNumber;

    @JsonProperty("hearingRequestID")
    private String hearingRequestId;

    private HmcStatus status;

    private LocalDateTime timestamp;

    private String hearingGroupRequestId;

    private LocalDateTime partiesNotified;

}
