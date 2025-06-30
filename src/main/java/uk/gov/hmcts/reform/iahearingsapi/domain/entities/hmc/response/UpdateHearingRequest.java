package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.CaseDetailsHearing;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.HearingRequestDetails;
import uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.PartyDetailsModel;
import uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc.HearingDetails;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
public class UpdateHearingRequest extends CreateHearingRequest {

    @NotNull
    private HearingRequestDetails requestDetails;

    public UpdateHearingRequest(@NotNull HearingDetails hearingDetails,
                                @NotNull CaseDetailsHearing caseDetails,
                                @Valid List<PartyDetailsModel> partyDetails,
                                HearingRequestDetails requestDetails) {
        super(hearingDetails, caseDetails, partyDetails);
        this.requestDetails = requestDetails;
    }
}
