package uk.gov.hmcts.reform.iahearingsapi.domain.entities.bail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.CaseFlagDetail;

import java.util.Collections;
import java.util.List;

@Value
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class BailStrategicCaseFlag {

    public static final String ROLE_ON_CASE_APPLICANT = "Applicant";
    public static final String ROLE_ON_CASE_FCS = "FCS";

    String partyName;
    String roleOnCase;
    @JsonProperty("details")
    List<CaseFlagDetail> details;

    public BailStrategicCaseFlag(String partyFullName, String roleOnCase) {
        this.partyName = partyFullName;
        this.roleOnCase = roleOnCase;
        this.details = Collections.emptyList();
    }

    public BailStrategicCaseFlag() {
        this.details = Collections.emptyList();
        this.partyName = null;
        this.roleOnCase = null;
    }
}
