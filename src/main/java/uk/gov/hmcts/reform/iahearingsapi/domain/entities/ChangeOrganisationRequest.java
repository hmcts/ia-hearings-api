package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeOrganisationRequest {

    @JsonProperty("CaseRoleId")
    private DynamicList caseRoleId;

    @JsonProperty("RequestTimestamp")
    private String requestTimestamp;

    @JsonProperty("ApprovalStatus")
    private String approvalStatus;

}
