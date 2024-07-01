package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseLinkInfo {
    private String caseNameHmctsInternal;
    private String caseReference;
    private String ccdCaseType;
    private String ccdJurisdiction;
    private String state;
    private List<CaseLinkDetails> linkDetails;
}
