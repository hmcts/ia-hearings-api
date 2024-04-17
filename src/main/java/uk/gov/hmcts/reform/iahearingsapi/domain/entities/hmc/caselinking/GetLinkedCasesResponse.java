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
public class GetLinkedCasesResponse {
    private boolean hasMoreRecords;
    private List<CaseLinkInfo> linkedCases;
}
