package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestDetails {

    @NotNull
    private Integer versionNumber;
}
