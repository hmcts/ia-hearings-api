package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsylumCaseHearingDecision {
    LocalDateTime decisionDate;
    AsylumCaseHearingOutcome hearingOutcome;
}
