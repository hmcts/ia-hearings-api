package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Setter
@Getter
public class UnNotifiedHearingsResponse {

    private List<String> hearingIds;

    @NotNull
    private Long totalFound;
}
