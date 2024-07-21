package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseLink;

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CaseLinksResource {
    private List<CaseLink> caseLinks;
}
