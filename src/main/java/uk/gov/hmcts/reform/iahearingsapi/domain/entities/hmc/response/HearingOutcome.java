package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class HearingOutcome {

    private String hearingType;

    private boolean hearingFinalFlag;

    private String hearingResult;

    private String hearingResultReasonType;

    private String hearingResultDate;
}
