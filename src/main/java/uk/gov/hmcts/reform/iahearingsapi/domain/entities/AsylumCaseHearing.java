package uk.gov.hmcts.reform.iahearingsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class AsylumCaseHearing {
    String hearingId;
    String nextHearingDate;
    AsylumCaseHearingDecision decision;
}
