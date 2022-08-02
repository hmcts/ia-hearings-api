package uk.gov.hmcts.reform.iahearingsapi.domain.model.single.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class HmcUpdateResponse {
    private Long hearingRequestId;

    private HmcStatus status;

    private LocalDateTime timeStamp;

    private Long versionNumber;
}
