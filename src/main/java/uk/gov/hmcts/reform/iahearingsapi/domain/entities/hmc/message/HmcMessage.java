package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.message.HearingUpdate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HmcMessage {
    @NonNull
    private String hmctsServiceCode;

    @NonNull
    @JsonProperty("caseRef")
    private Long caseId;

    @NonNull
    @JsonProperty("hearingID")
    private String hearingId;

    @NonNull
    private HearingUpdate hearingUpdate;
}
