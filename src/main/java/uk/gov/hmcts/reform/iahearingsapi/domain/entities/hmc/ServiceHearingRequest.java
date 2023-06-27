package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ServiceHearingRequest {
    @NonNull
    private String caseId;
    private String hearingId;
}
