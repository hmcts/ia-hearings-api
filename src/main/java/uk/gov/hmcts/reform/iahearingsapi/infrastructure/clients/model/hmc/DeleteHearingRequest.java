package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeleteHearingRequest {

    @NotEmpty(message = "Cancellation Reason code details are not present")
    private List<@Size(min = 1, max = 100, message = "Cancellation Reason code "
        + "length must be at least 1 and no greater than 100 characters") String>
        cancellationReasonCodes;

}
