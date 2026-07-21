package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "hearingLinkDataWith")
@NoArgsConstructor
@AllArgsConstructor
public class HearingLinkData {
    public String caseReference;
    public String caseName;
    public List<String> reasonsForLink;
}
