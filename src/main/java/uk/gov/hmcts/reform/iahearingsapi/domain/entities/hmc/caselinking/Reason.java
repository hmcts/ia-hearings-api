package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.caselinking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reason {

    private String reasonCode;
    private String otherDescription;

}
