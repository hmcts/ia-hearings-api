package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import lombok.Data;
import org.springframework.lang.NonNull;

@Data
public class HearingRequestPayload {

    @NonNull
    private String caseReference;
    private String hearingId;
}
