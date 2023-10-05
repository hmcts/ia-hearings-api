package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingRequestDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class UpdateHearingRequest {

    @NotNull
    private HearingDetails hearingDetails;

    @NotNull
    private CaseDetailsHearing caseDetails;

    @Valid
    private List<PartyDetailsModel> partyDetails;

    @NotNull
    private HearingRequestDetails requestDetails;
}
