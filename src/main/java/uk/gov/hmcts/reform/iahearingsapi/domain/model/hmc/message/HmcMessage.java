package uk.gov.hmcts.reform.iahearingsapi.domain.model.hmc.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

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

    @Override
    public String toString() {
        return String.format("\nHmcts Service Code: %s\nCase Ref: %s\nHearing ID: %s\n", hmctsServiceCode, caseId, hearingId);
    }
}
