package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder(builderMethodName = "hearingLinkDataWith")
@NoArgsConstructor
public class HearingLinkData {
    @Getter
    @Setter
    private String caseReference;
    @Getter
    @Setter
    private String caseName;
    private List<String> reasonsForLink;

    public HearingLinkData(String caseReference, String caseName, List<String> reasonsForLink) {
        this.caseReference = caseReference;
        this.caseName = caseName;
        this.reasonsForLink = reasonsForLink == null ? new ArrayList<>() : new ArrayList<>(reasonsForLink);
    }

    public List<String> getReasonsForLink() {
        return reasonsForLink == null ? Collections.emptyList() : Collections.unmodifiableList(reasonsForLink);
    }

    public void setReasonsForLink(List<String> reasonsForLink) {
        this.reasonsForLink = reasonsForLink == null ? new ArrayList<>() : new ArrayList<>(reasonsForLink);
    }
}
