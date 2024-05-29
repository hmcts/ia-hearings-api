package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategicCaseFlag {

    public static final String ROLE_ON_CASE_APPELLANT = "Appellant";
    public static final String ROLE_ON_CASE_WITNESS = "Witness";

    String partyName;
    String roleOnCase;
    @JsonProperty("details")
    List<CaseFlagDetail> details;

    public StrategicCaseFlag(String partyName, String roleOnCase, List<CaseFlagDetail> details) {
        this.partyName = partyName;
        this.roleOnCase = roleOnCase;
        this.details = details != null ? details : Collections.emptyList();
    }

    public StrategicCaseFlag(List<CaseFlagDetail> details) {
        this(null, null, details);
    }

    public StrategicCaseFlag() {
        this(null, null, Collections.emptyList());
    }
}
