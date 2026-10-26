package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingOutcome {

    private String hearingType;

    private String hearingFinalFlag;

    private String hearingResult;

    private String hearingResultReasonType;

    private String hearingResultDate;
}
