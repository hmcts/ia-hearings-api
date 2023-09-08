package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class HearingsResponse {

    private String hmctsServiceCode;

    private String caseRef;

    private List<CaseHearing> caseHearings;

}
