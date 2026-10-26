package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.ServiceData;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HmcStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HearingActualsResponse {

    private HmcStatus hmcStatus;

    private CaseDetailsHearing caseDetailsHearing;

    private HearingPlanned hearingPlanned;

    private HearingActuals hearingActuals;
}
