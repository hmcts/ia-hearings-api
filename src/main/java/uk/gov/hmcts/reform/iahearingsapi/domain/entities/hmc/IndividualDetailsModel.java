package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndividualDetailsModel {

    private String title;
    private String firstName;
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
