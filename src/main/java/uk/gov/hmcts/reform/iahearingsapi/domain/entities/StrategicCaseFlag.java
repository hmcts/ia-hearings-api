package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import java.util.Collections;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Value
@Getter
@Setter
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategicCaseFlag {

    public static String ROLE_ON_CASE_APPELLANT = "Appellant";
    public static String ROLE_ON_CASE_WITNESS = "Witness";

    String partyName;
    String roleOnCase;

    @JsonProperty("details")
    List<CaseFlagDetail> details;

    public StrategicCaseFlag(String partyNameForDisplay, String roleOnCase) {
        this.partyName = partyNameForDisplay;
        this.roleOnCase = roleOnCase;
        this.details = Collections.emptyList();
    }

    public StrategicCaseFlag(String partyNameForDisplay, String roleOnCase, List<CaseFlagDetail> details) {
        this.partyName = partyNameForDisplay;
        this.roleOnCase = roleOnCase;
        this.details = details;
    }

    public StrategicCaseFlag() {
        this.details = Collections.emptyList();
        this.partyName = null;
        this.roleOnCase = null;
    }
}
