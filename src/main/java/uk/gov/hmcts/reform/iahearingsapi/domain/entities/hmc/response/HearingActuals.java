package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingActuals {

    private HearingOutcome hearingOutcome;

    private List<ActualHearingDay> actualHearingDays;
}
