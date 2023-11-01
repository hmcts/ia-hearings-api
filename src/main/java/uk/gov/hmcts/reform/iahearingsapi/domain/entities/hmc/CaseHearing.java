package uk.gov.hmcts.reform.iahearingsapi.domain.entities.hmc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseHearing {

    private List<String> hearingChannels;
    private List<HearingDaySchedule> hearingDaySchedule;
    private String hearingGroupRequestId;
    @JsonProperty("hearingID")
    private String hearingRequestId;
    private Boolean hearingIsLinkedFlag;
    private String hearingListingStatus;
    private LocalDateTime hearingRequestDateTime;
    private String hearingType;
    private HmcStatus hmcStatus;
    private LocalDateTime lastResponseReceivedDateTime;
    private String listAssistCaseStatus;
    private String requestVersion;


    public String getHearingTypeDescription() {
        return switch (hearingType) {
            case "BFA1-CMR" -> "Case Management Review";
            case "BFA1-COS" -> "Costs";
            case "BFA1-BAI" -> "Bail";
            case "BFA1-SUB" -> "Substantive";
            default -> "BBA3-substantive";
        };
    }
}
