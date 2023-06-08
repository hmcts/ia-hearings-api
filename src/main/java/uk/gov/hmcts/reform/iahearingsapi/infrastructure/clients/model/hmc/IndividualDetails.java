package uk.gov.hmcts.reform.iahearingsapi.infrastructure.clients.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndividualDetails {

    private String title;

    private String firstName;

    private String lastName;

    private String preferredHearingChannel;

    private String interpreterLanguage;

    private List<String> reasonableAdjustments;

    private boolean vulnerableFlag;

    private String vulnerabilityDetails;

    private List<String> hearingChannelEmail;

    private List<String> hearingChannelPhone;

    private List<RelatedParty> relatedParties;

    private String custodyStatus;

    private String otherReasonableAdjustmentDetails;

}
