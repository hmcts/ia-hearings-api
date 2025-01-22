package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsylumCaseHearingDecision {
    String decisionDate;
    AsylumCaseHearingOutcome hearingOutcome;
}
