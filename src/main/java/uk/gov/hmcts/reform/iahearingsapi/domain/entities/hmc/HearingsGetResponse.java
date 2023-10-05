package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class HearingsGetResponse {

    private String caseRef;

    private List<CaseHearing> caseHearings;

    private String hmctsServiceCode;

}
