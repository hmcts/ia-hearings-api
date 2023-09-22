package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartiesNotifiedResponses {

    private String hearingID;

    private List<PartiesNotifiedResponse> responses;

}