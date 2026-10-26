package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingRequestDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateHearingActualsRequest {
    private HearingOutcome hearingOutcome;

    private List<ActualHearingDay> actualHearingDays;
}
