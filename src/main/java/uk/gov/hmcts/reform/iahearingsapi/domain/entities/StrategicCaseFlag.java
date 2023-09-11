package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StrategicCaseFlag {

    public static String ROLE_ON_CASE_APPELLANT = "Appellant";
    public static String ROLE_ON_CASE_WITNESS = "Witness";

    String partyName;
    String roleOnCase;
    @JsonProperty("details")
    List<CaseFlagDetail> details;

    public StrategicCaseFlag(List<CaseFlagDetail> details) {
        this.partyName = null;
        this.roleOnCase = null;
        this.details = details;
    }
}
