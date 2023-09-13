package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndividualDetailsModel {

    private String title;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    private String preferredHearingChannel;
    private String interpreterLanguage;
    private List<String> reasonableAdjustments;
    private Boolean vulnerableFlag;
    private String vulnerabilityDetails;
    private List<String> hearingChannelEmail;
    private List<String> hearingChannelPhone;
    private List<RelatedPartiesModel> relatedParties;
    private String custodyStatus;
    private String otherReasonableAdjustmentDetails;
}
